/*
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.cs.admin.rest.api.converter;

import lombok.RequiredArgsConstructor;
import org.niis.xroad.cs.admin.api.paging.Page;
import org.niis.xroad.cs.openapi.model.PagingMetadataDto;
import org.niis.xroad.cs.openapi.model.PagingSortingParametersDto;
import org.springframework.stereotype.Component;

import static java.lang.Math.toIntExact;

@Component
@RequiredArgsConstructor
public class PagingMetadataConverter {
    public PagingMetadataDto convert(Page<?> page,
                                     PagingSortingParametersDto pagingSorting) {
        PagingMetadataDto meta = new PagingMetadataDto();
        meta.setTotalItems(toIntExact(page.getTotalElements()));
        meta.setLimit(pagingSorting.getLimit());
        meta.setOffset(pagingSorting.getOffset());
        meta.setItems(page.getNumberOfElements());
        return meta;
    }
}
