/*
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
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

import ee.ria.xroad.common.SystemProperties;
import ee.ria.xroad.common.cert.CertChainFactory;
import ee.ria.xroad.common.conf.globalconf.GlobalConfProvider;
import ee.ria.xroad.common.conf.serverconf.ServerConfProvider;
import ee.ria.xroad.common.db.HibernateUtil;
import ee.ria.xroad.common.opmonitoring.OpMonitoringDaemonHttpClient;
import ee.ria.xroad.common.opmonitoring.OpMonitoringSystemProperties;
import ee.ria.xroad.common.util.CryptoUtils;
import ee.ria.xroad.common.util.StartStop;
import ee.ria.xroad.common.util.TimeUtils;
import ee.ria.xroad.proxy.antidos.AntiDosConnector;
import ee.ria.xroad.proxy.conf.KeyConfProvider;
import ee.ria.xroad.proxy.util.SSLContextUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Server proxy that handles requests of client proxies.
 */
@Slf4j
public class ServerProxy implements StartStop {

    private static final int ACCEPTOR_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());

    private static final int IDLE_MONITOR_TIMEOUT = 50;

    private static final int IDLE_MONITOR_INTERVAL = 100;

    // SSL session timeout in seconds
    private static final int SSL_SESSION_TIMEOUT = 600;

    private static final int CONNECTOR_SO_LINGER_MILLIS = SystemProperties.getServerProxyConnectorSoLinger();
    private static final String CLIENT_PROXY_CONNECTOR_NAME = "ClientProxyConnector";

    private final Server server = new Server();

    private final GlobalConfProvider globalConfProvider;
    private final KeyConfProvider keyConfProvider;
    private final ServerConfProvider serverConfProvider;
    private final CertChainFactory certChainFactory;

    private CloseableHttpClient client;
    private IdleConnectionMonitorThread connMonitor;

    private String listenAddress;

    private CloseableHttpClient opMonitorClient;

    public ServerProxy(GlobalConfProvider globalConfProvider, KeyConfProvider keyConfProvider, ServerConfProvider serConfProvider,
                       CertChainFactory certChainFactory) throws Exception {
        this(globalConfProvider, keyConfProvider, serConfProvider, certChainFactory, SystemProperties.getServerProxyListenAddress());
    }

    public ServerProxy(GlobalConfProvider globalConfProvider, KeyConfProvider keyConfProvider, ServerConfProvider serConfProvider,
                       CertChainFactory certChainFactory, String listenAddress) throws Exception {
        this.globalConfProvider = globalConfProvider;
        this.keyConfProvider = keyConfProvider;
        this.serverConfProvider = serConfProvider;
        this.certChainFactory = certChainFactory;
        this.listenAddress = listenAddress;

        configureServer();

        createClient();
        createOpMonitorClient();
        createConnectors();
        createHandlers();
    }

    private void configureServer() throws Exception {
        log.trace("configureServer()");

        Path file = Paths.get(SystemProperties.getJettyServerProxyConfFile());

        log.debug("Configuring server from {}", file);

        new XmlConfiguration(ResourceFactory.root().newResource(file)).configure(server);

        final var writer = new Slf4jRequestLogWriter();
        writer.setLoggerName(getClass().getPackage().getName() + ".RequestLog");
        final var reqLog = new CustomRequestLog(writer, CustomRequestLog.EXTENDED_NCSA_FORMAT);
        server.setRequestLog(reqLog);
    }

    private void createClient() throws Exception {
        log.trace("createClient()");

        HttpClientCreator creator = new HttpClientCreator(serverConfProvider);

        connMonitor = new IdleConnectionMonitorThread(creator.getConnectionManager());
        connMonitor.setIntervalMilliseconds(IDLE_MONITOR_INTERVAL);
        connMonitor.setConnectionIdleTimeMilliseconds(IDLE_MONITOR_TIMEOUT);

        client = creator.getHttpClient();
    }

    private void createOpMonitorClient() throws Exception {
        opMonitorClient = OpMonitoringDaemonHttpClient.createHttpClient(serverConfProvider.getSSLKey(),
                TimeUtils.secondsToMillis(OpMonitoringSystemProperties.getOpMonitorServiceConnectionTimeoutSeconds()),
                TimeUtils.secondsToMillis(OpMonitoringSystemProperties.getOpMonitorServiceSocketTimeoutSeconds()));
    }

    private void createConnectors() throws Exception {
        log.trace("createConnectors()");

        int port = SystemProperties.getServerProxyListenPort();

        ServerConnector connector = SystemProperties.isSslEnabled()
                ? createClientProxySslConnector() : createClientProxyConnector();

        connector.setName(CLIENT_PROXY_CONNECTOR_NAME);
        connector.setPort(port);
        connector.setHost(listenAddress);

        connector.setIdleTimeout(SystemProperties.getServerProxyConnectorInitialIdleTime());

        connector.getConnectionFactories().stream()
                .filter(HttpConnectionFactory.class::isInstance)
                .map(HttpConnectionFactory.class::cast)
                .forEach(httpCf -> {
                    httpCf.getHttpConfiguration().setSendServerVersion(false);
                    Optional.ofNullable(httpCf.getHttpConfiguration().getCustomizer(SecureRequestCustomizer.class))
                            .ifPresent(customizer -> {
                                customizer.setSniHostCheck(false);
                            });
                });

        server.addConnector(connector);

        log.info("ClientProxy {} created ({}:{})", connector.getClass().getSimpleName(), listenAddress, port);
    }

    private void createHandlers() {
        log.trace("createHandlers()");

        ServerProxyHandler proxyHandler = new ServerProxyHandler(globalConfProvider, keyConfProvider, serverConfProvider,
                certChainFactory, client, opMonitorClient);

        var handler = new Handler.Sequence();
        handler.addHandler(proxyHandler);

        server.setHandler(handler);
    }

    @Override
    public void start() throws Exception {
        log.trace("start()");

        server.start();
        connMonitor.start();
    }

    @Override
    public void join() throws InterruptedException {
        log.trace("join()");

        if (server.getThreadPool() != null) {
            server.join();
        }
    }

    @Override
    public void stop() throws Exception {
        log.trace("stop()");

        connMonitor.shutdown();
        client.close();
        opMonitorClient.close();
        server.stop();

        HibernateUtil.closeSessionFactories();
    }

    /**
     * Close idle connections.
     */
    public void closeIdleConnections() {
        connMonitor.closeNow();
    }

    private ServerConnector createClientProxyConnector() {
        return SystemProperties.isAntiDosEnabled()
                ? new AntiDosConnector(globalConfProvider, server, ACCEPTOR_COUNT) : new ServerConnector(server, ACCEPTOR_COUNT, -1);
    }

    private ServerConnector createClientProxySslConnector() throws Exception {
        var cf = new SslContextFactory.Server();
        cf.setNeedClientAuth(true);
        cf.setIncludeProtocols(CryptoUtils.SSL_PROTOCOL);
        cf.setIncludeCipherSuites(SystemProperties.getXroadTLSCipherSuites());
        cf.setSessionCachingEnabled(true);
        cf.setSslSessionTimeout(SSL_SESSION_TIMEOUT);
        cf.setSslContext(SSLContextUtil.createXroadSSLContext(globalConfProvider, keyConfProvider));

        return SystemProperties.isAntiDosEnabled()
                ? new AntiDosConnector(globalConfProvider, server, ACCEPTOR_COUNT, cf)
                : new ServerConnector(server, ACCEPTOR_COUNT, -1, cf);
    }

}
