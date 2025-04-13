package com.example.demo.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    @Autowired
    public AuthFilter(@Lazy JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER_NAME);
        String token = extractToken(header);

        if (!token.isEmpty()) {
            authenticateUser(token, request, response);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(String header) {
        if (validateHeaderName(header)) {
            return header.substring(7);
        }
        return "";
    }

    private boolean validateHeaderName(@Nullable String header) {
        return header != null && header.startsWith(BEARER_PREFIX);
    }

    private void authenticateUser(String token, HttpServletRequest request, HttpServletResponse response) {
        String username = jwtTokenProvider.getUsernameFromToken(token);
        log.info("Username is: {}", username);
        log.info("Token: {}", token);

        if (!isAuthenticated(username)) {
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token)) {
                setSecurityContext(userDetails, request);
                response.setHeader(HEADER_NAME, BEARER_PREFIX + token);
            }
        }
    }

    private boolean isAuthenticated(String username) {
        return username.isEmpty() && SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }
}
