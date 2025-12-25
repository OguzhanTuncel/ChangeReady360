package com.changeready.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		try {
			String jwt = getJwtFromRequest(request);

			if (StringUtils.hasText(jwt)) {
				if (tokenProvider.validateToken(jwt)) {
					String email = tokenProvider.getEmailFromToken(jwt);
					UserPrincipal userPrincipal = userDetailsService.loadUserByUsername(email);

					if (userPrincipal != null && userPrincipal.isEnabled()) {
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userPrincipal, null, userPrincipal.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
						// SEC-010: Log user ID instead of email for privacy
						logger.debug("Successfully authenticated user with ID: {}", userPrincipal.getId());
					} else {
						// SEC-010: Don't log email for security/privacy
						logger.warn("User principal is null or disabled");
					}
				} else {
					logger.debug("Invalid JWT token");
				}
			} else {
				logger.debug("No JWT token found in request");
			}
		} catch (Exception ex) {
			// SEC-010: Don't log exception details that might contain tokens
			logger.error("Could not set user authentication in security context");
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}

