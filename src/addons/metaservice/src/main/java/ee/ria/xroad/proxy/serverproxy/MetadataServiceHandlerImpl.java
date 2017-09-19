/**
 * The MIT License
 * Copyright (c) 2015 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ee.ria.xroad.proxy.serverproxy;

import ee.ria.xroad.common.CodedException;
import ee.ria.xroad.common.conf.serverconf.ServerConf;
import ee.ria.xroad.common.conf.serverconf.ServerConfDatabaseCtx;
import ee.ria.xroad.common.conf.serverconf.dao.WsdlDAOImpl;
import ee.ria.xroad.common.conf.serverconf.model.WsdlType;
import ee.ria.xroad.common.identifier.ServiceId;
import ee.ria.xroad.common.message.*;
import ee.ria.xroad.common.message.SoapUtils.SOAPCallback;
import ee.ria.xroad.common.metadata.MethodListType;
import ee.ria.xroad.common.metadata.ObjectFactory;
import ee.ria.xroad.common.opmonitoring.OpMonitoringData;
import ee.ria.xroad.common.util.MimeTypes;
import ee.ria.xroad.proxy.common.WsdlRequestData;
import ee.ria.xroad.proxy.protocol.ProxyMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.*;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static ee.ria.xroad.common.ErrorCodes.X_INVALID_REQUEST;
import static ee.ria.xroad.common.ErrorCodes.X_UNKNOWN_SERVICE;
import static ee.ria.xroad.common.metadata.MetadataRequests.*;

@Slf4j
class MetadataServiceHandlerImpl implements ServiceHandler {

    static final JAXBContext JAXB_CTX = initJaxbCtx();
    static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public static final String WSDL_ENDPOINT_ADDRESS = "http://example.org/xroad-endpoint";

    private final ByteArrayOutputStream responseOut =
            new ByteArrayOutputStream();

    private SoapMessageImpl requestMessage;
    private SoapMessageEncoder responseEncoder;

    private HttpClientCreator wsdlHttpClientCreator = new HttpClientCreator();

    private static final SAXTransformerFactory TRANSFORMER_FACTORY = createSaxTransformerFactory();

    private static SAXTransformerFactory createSaxTransformerFactory() {
        try {
            SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            return factory;
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("unable to create SAX transformer factory", e);
        }
    }

    @Override
    public boolean shouldVerifyAccess() {
        return false;
    }

    @Override
    public boolean shouldVerifySignature() {
        return true;
    }

    @Override
    public boolean shouldLogSignature() {
        return true;
    }

    @Override
    @SneakyThrows
    public boolean canHandle(ServiceId requestServiceId,
            ProxyMessage requestProxyMessage) {
        requestMessage = requestProxyMessage.getSoap();

        switch (requestServiceId.getServiceCode()) {
            case LIST_METHODS: // $FALL-THROUGH$
            case ALLOWED_METHODS: // $FALL-THROUGH$
            case GET_WSDL:
                requestMessage = (SoapMessageImpl) new SoapParserImpl().parse(
                        requestProxyMessage.getSoapContentType(),
                        requestProxyMessage.getSoapContent());
                return true;
            default:
                return false;
        }
    }

    @Override
    public void startHandling(HttpServletRequest servletRequest,
            ProxyMessage proxyRequestMessage, HttpClient opMonitorClient,
            OpMonitoringData opMonitoringData) throws Exception {

        final String serviceCode = requestMessage.getService().getServiceCode();

        // Only get wsdl needs to be a multipart message
        responseEncoder = GET_WSDL.equals(serviceCode)
                ? new MultipartSoapMessageEncoder(responseOut)
                : new SimpleSoapEncoder(responseOut);

        // It's required that in case of metadata service (where SOAP message is
        // not forwarded) the requestOutTs must be equal with the requestInTs
        // and the responseInTs must be equal with the responseOutTs.
        opMonitoringData.setRequestOutTs(opMonitoringData.getRequestInTs());
        opMonitoringData.setAssignResponseOutTsToResponseInTs(true);


        switch (serviceCode) {
            case LIST_METHODS:
                handleListMethods(requestMessage);
                return;
            case ALLOWED_METHODS:
                handleAllowedMethods(requestMessage);
                return;
            case GET_WSDL:
                handleGetWsdl(requestMessage);
                return;
            default: // do nothing
                return;
        }
    }

    @Override
    public void finishHandling() throws Exception {
        // nothing to do
    }

    @Override
    public String getResponseContentType() {
        return responseEncoder.getContentType();
    }

    @Override
    public InputStream getResponseContent() throws Exception {
        return new ByteArrayInputStream(responseOut.toByteArray());
    }

    // ------------------------------------------------------------------------

    private void handleListMethods(SoapMessageImpl request) throws Exception {
        log.trace("handleListMethods()");

        MethodListType methodList = OBJECT_FACTORY.createMethodListType();
        methodList.getService().addAll(
                ServerConf.getAllServices(
                        request.getService().getClientId()));

        SoapMessageImpl result = createMethodListResponse(request,
                OBJECT_FACTORY.createListMethodsResponse(methodList));
        responseEncoder.soap(result, new HashMap<>());
    }

    private void handleAllowedMethods(SoapMessageImpl request)
            throws Exception {
        log.trace("handleAllowedMethods()");

        MethodListType methodList = OBJECT_FACTORY.createMethodListType();
        methodList.getService().addAll(
                ServerConf.getAllowedServices(
                        request.getService().getClientId(),
                        request.getClient()));

        SoapMessageImpl result = createMethodListResponse(request,
                OBJECT_FACTORY.createAllowedMethodsResponse(methodList));
        responseEncoder.soap(result, new HashMap<>());
    }

    private void handleGetWsdl(SoapMessageImpl request) throws Exception {
        log.trace("handleGetWsdl()");

        Unmarshaller um = JaxbUtils.createUnmarshaller(WsdlRequestData.class);

        WsdlRequestData requestData = um.unmarshal(
                SoapUtils.getFirstChild(request.getSoap().getSOAPBody()),
                WsdlRequestData.class).getValue();

        if (StringUtils.isBlank(requestData.getServiceCode())) {
            throw new CodedException(X_INVALID_REQUEST,
                    "Missing serviceCode in message body");
        }

        ServiceId serviceId = requestData.toServiceId(request.getService().getClientId());
        String url = getWsdlUrl(serviceId);
        if (url == null) {
            throw new CodedException(X_UNKNOWN_SERVICE,
                    "Could not find wsdl URL for service %s",
                    requestData.toServiceId(
                            request.getService().getClientId()));
        }

        log.info("Downloading WSDL from URL: {}", url);
        try (InputStream in = modifyWsdl(getWsdl(url, serviceId))) {
            responseEncoder.soap(SoapUtils.toResponse(request), new HashMap<>());
            responseEncoder.attachment(MimeTypes.TEXT_XML, in, null);
        }
    }

    // ------------------------------------------------------------------------

    private String getWsdlUrl(ServiceId service) throws Exception {
        return ServerConfDatabaseCtx.doInTransaction(session -> {
            WsdlType wsdl = new WsdlDAOImpl().getWsdl(session, service);
            return wsdl != null ? wsdl.getUrl() : null;
        });
    }

    private static SoapMessageImpl createMethodListResponse(
            SoapMessageImpl requestMessage,
            final JAXBElement<MethodListType> methodList) throws Exception {
        SoapMessageImpl responseMessage = SoapUtils.toResponse(requestMessage,
                new SOAPCallback() {
            @Override
            public void call(SOAPMessage soap) throws Exception {
                soap.getSOAPBody().removeContents();
                marshal(methodList, soap.getSOAPBody());
            }
        });

        return responseMessage;
    }

    private static void marshal(Object object, Node out) throws Exception {
        Marshaller marshaller = JAXB_CTX.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.marshal(object, out);
    }

    private static JAXBContext initJaxbCtx() {
        try {
            return JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * We need a lexicalHandler for the reader, to catch XML comments.
     * This just delegates to serializer (TransformerHandler) which is
     * a lexicalHandler, too
     */
    private static class CommentsHandler extends DefaultHandler2 {
        private LexicalHandler serializer;
        protected CommentsHandler(LexicalHandler serializer) {
            super();
            this.serializer = serializer;
        }
        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            serializer.comment(ch, start, length);
        }
    }

    /**
     * reads a WSDL from input stream, modifies it and returns input stream to the result
     * @param wsdl
     * @return
     */
    private InputStream modifyWsdl(InputStream wsdl) {
        try {
            TransformerHandler serializer = TRANSFORMER_FACTORY.newTransformerHandler();
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            serializer.setResult(result);

            OverwriteAttributeFilter filter = getModifyWsdlFilter();
            filter.setContentHandler(serializer);

            XMLReader xmlreader = XMLReaderFactory.createXMLReader();
            xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler",
                    new CommentsHandler(serializer));
            xmlreader.setContentHandler(filter);

            // parse XML, filter it, put end result to a String
            xmlreader.parse(new InputSource(wsdl));
            String resultString = writer.toString();
            log.debug("result of WSDL cleanup: {}", resultString);

            // offer InputStream into processed String
            return new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | SAXException | TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    protected OverwriteAttributeFilter getModifyWsdlFilter() {
        return OverwriteAttributeFilter.createOverwriteSoapAddressFilter(WSDL_ENDPOINT_ADDRESS);
    }

    private InputStream getWsdl(String url, ServiceId serviceId)
            throws HttpClientCreator.HttpClientCreatorException, URISyntaxException, IOException {

        HttpClient client = wsdlHttpClientCreator.getHttpClient();

        HttpContext httpContext = new BasicHttpContext();

        // ServerMessageProcessor uses the same method to pass the ServiceId to CustomSSLSocketFactory
        httpContext.setAttribute(ServiceId.class.getName(), serviceId);

        HttpResponse response = client.execute(new HttpGet(new URI(url)), httpContext);

        StatusLine statusLine = response.getStatusLine();

        if (HttpStatus.SC_OK != statusLine.getStatusCode()) {
            throw new RuntimeException("Received HTTP error: "
                    + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
        }

        return response.getEntity().getContent();
    }
}