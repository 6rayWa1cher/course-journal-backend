package com.a6raywa1cher.coursejournalbackend.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

public class CourseTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_TYPE = "ctbearer";

    private final AuthenticationManager authenticationManager;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public CourseTokenAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    private Authentication check(HttpServletRequest request) throws AuthenticationException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return null;
        }
        String[] split = header.split(" ");
        if (split.length != 2 || !split[0].toLowerCase(Locale.ROOT).equals(AUTH_TYPE) || header.contains("."))
            return null;
        String token = split[1];
        return new CourseTokenAuthentication(token, request.getRemoteAddr());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = check(request);
            if (authentication != null) {
                Authentication auth = authenticationManager.authenticate(authentication);
                SecurityContext sc = SecurityContextHolder.getContext();
                if (sc == null) {
                    sc = SecurityContextHolder.createEmptyContext();
                    SecurityContextHolder.setContext(sc);
                }
                sc.setAuthentication(auth);
            }
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
