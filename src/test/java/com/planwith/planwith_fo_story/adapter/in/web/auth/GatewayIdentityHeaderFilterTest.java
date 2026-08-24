package com.planwith.planwith_fo_story.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;

class GatewayIdentityHeaderFilterTest {

	@Test
	void exposesOnlyVerifiedGatewayMemberIdentity() throws Exception {
		GatewayIdentityHeaderFilter filter = new GatewayIdentityHeaderFilter(true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/stories/me");
		request.addHeader(GatewayIdentityHeaderFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		request.addHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS, "spoofed");
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		HttpServletRequest trustedRequest = (HttpServletRequest) chain.getRequest();
		assertThat(trustedRequest.getHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS))
				.isEqualTo("11111111-1111-1111-1111-111111111111");
	}

	@Test
	void removesUnverifiedMemberIdentity() throws Exception {
		GatewayIdentityHeaderFilter filter = new GatewayIdentityHeaderFilter(true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/stories");
		request.addHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS, "spoofed");
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(((HttpServletRequest) chain.getRequest()).getHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS))
				.isNull();
	}
}
