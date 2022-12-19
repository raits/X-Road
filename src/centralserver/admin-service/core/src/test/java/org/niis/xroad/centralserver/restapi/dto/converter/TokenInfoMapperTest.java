/*
 * The MIT License
 * <p>
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

package org.niis.xroad.centralserver.restapi.dto.converter;

import ee.ria.xroad.signer.protocol.dto.TokenInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.niis.xroad.centralserver.restapi.service.TokenActionsResolver;
import org.niis.xroad.cs.admin.api.dto.PossibleAction;
import org.niis.xroad.cs.admin.api.dto.TokenStatus;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static ee.ria.xroad.signer.protocol.dto.TokenStatusInfo.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenInfoMapperTest {

    @Mock
    private TokenActionsResolver tokenActionsResolver;

    private final TokenInfoMapper tokenInfoMapper = new TokenInfoMapperImpl();

    @BeforeEach
    void setup() {
        tokenInfoMapper.tokenActionsResolver = tokenActionsResolver;
    }

    @Test
    void toTarget() {
        final TokenInfo tokenInfo = createTokenInfo();
        final EnumSet<PossibleAction> possibleActions = mock(EnumSet.class);
        when(tokenActionsResolver.resolveActions(tokenInfo)).thenReturn(possibleActions);

        final org.niis.xroad.cs.admin.api.dto.TokenInfo result = tokenInfoMapper.toTarget(tokenInfo);

        assertThat(result.getId()).isEqualTo("TOKEN_ID");
        assertThat(result.getType()).isEqualTo("type");
        assertThat(result.getFriendlyName()).isEqualTo("TOKEN_FRIENDLY_NAME");
        assertThat(result.getSerialNumber()).isEqualTo("TOKEN_SERIAL_NUMBER");
        assertThat(result.getLabel()).isEqualTo("label");
        assertThat(result.getSlotIndex()).isEqualTo(13);
        assertThat(result.getStatus()).isEqualTo(TokenStatus.OK);
        assertThat(result.isReadOnly()).isFalse();
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.isActive()).isFalse();
        assertThat(result.getPossibleActions()).isEqualTo(possibleActions);

        verify(tokenActionsResolver).resolveActions(tokenInfo);
    }

    private TokenInfo createTokenInfo() {
        return new TokenInfo(
                "type", "TOKEN_FRIENDLY_NAME", "TOKEN_ID", false, true,
                false, "TOKEN_SERIAL_NUMBER", "label", 13, OK, List.of(), Map.of("key", "value")
        );
    }

}
