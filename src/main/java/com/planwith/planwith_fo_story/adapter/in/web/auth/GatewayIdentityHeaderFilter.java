package com.planwith.planwith_fo_story.adapter.in.web.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayIdentityHeaderFilter extends OncePerRequestFilter {

	static final String AUTH_USER_ID = "X-Auth-User-Id";
	static final String MEMBER_UUID_ALIAS = "X-Member-UUID";

	private final boolean enabled;

	public GatewayIdentityHeaderFilter(@Value("${story.gateway-auth.enabled:true}") boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!enabled) {
			filterChain.doFilter(request, response);
			return;
		}
		filterChain.doFilter(new TrustedMemberRequest(request), response);
	}

	private static final class TrustedMemberRequest extends HttpServletRequestWrapper {

		private TrustedMemberRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getHeader(String name) {
			return MEMBER_UUID_ALIAS.equalsIgnoreCase(name) ? super.getHeader(AUTH_USER_ID) : super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			String value = getHeader(name);
			return value == null ? Collections.emptyEnumeration() : Collections.enumeration(Collections.singleton(value));
		}
	}
}
