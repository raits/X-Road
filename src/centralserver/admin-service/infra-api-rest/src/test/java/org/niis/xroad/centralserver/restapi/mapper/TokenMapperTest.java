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

package org.niis.xroad.centralserver.restapi.mapper;

import org.junit.jupiter.api.Test;
import org.niis.xroad.centralserver.openapi.model.PossibleActionDto;
import org.niis.xroad.centralserver.openapi.model.TokenDto;
import org.niis.xroad.centralserver.openapi.model.TokenStatusDto;
import org.niis.xroad.cs.admin.api.dto.TokenInfo;
import org.niis.xroad.cs.admin.api.dto.TokenStatus;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.niis.xroad.cs.admin.api.dto.PossibleAction.LOGIN;
import static org.niis.xroad.cs.admin.api.dto.PossibleAction.LOGOUT;

class TokenMapperTest {

    private final TokenMapper tokenMapper = new TokenMapperImpl();

    @Test
    void toTarget() {
        final TokenDto result = tokenMapper.toTarget(tokenInfo());
        assertThat(result.getActive()).isTrue();
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getId()).isEqualTo("id");
        assertThat(result.getLoggedIn()).isTrue();
        assertThat(result.getName()).isEqualTo("friendlyName");
        assertThat(result.getPossibleActions())
                .containsExactlyInAnyOrder(PossibleActionDto.LOGIN, PossibleActionDto.LOGOUT);
        assertThat(result.getSerialNumber()).isEqualTo("serialNumber");
        assertThat(result.getStatus()).isEqualTo(TokenStatusDto.OK);
    }

    private TokenInfo tokenInfo() {
        final TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setType("type");
        tokenInfo.setFriendlyName("friendlyName");
        tokenInfo.setId("id");
        tokenInfo.setReadOnly(true);
        tokenInfo.setAvailable(false);
        tokenInfo.setActive(true);
        tokenInfo.setSerialNumber("serialNumber");
        tokenInfo.setLabel("label");
        tokenInfo.setSlotIndex(123);
        tokenInfo.setStatus(TokenStatus.OK);
        tokenInfo.setPossibleActions(EnumSet.of(LOGIN, LOGOUT));

        return tokenInfo;
    }

}
