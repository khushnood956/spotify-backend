package com.spotify.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestUri = request.getRequestURI();
        final String method = request.getMethod();

        System.out.println("🚀 ========== JWT FILTER START ==========");
        System.out.println("📨 Request: " + method + " " + requestUri);
        System.out.println("🌐 Remote Address: " + request.getRemoteAddr());

        // Skip filter for certain paths (optional)
        if (shouldNotFilter(request)) {
            System.out.println("⏭️  Skipping JWT filter for public endpoint: " + requestUri);
            filterChain.doFilter(request, response);
            System.out.println("🏁 ========== JWT FILTER END (SKIPPED) ==========");
            return;
        }

        System.out.println("🔒 Protected endpoint - checking authentication");

        final String authHeader = request.getHeader("Authorization");
        System.out.println("🔑 Authorization Header: " + (authHeader != null ? authHeader : "NULL"));

        // Print all headers for debugging
        System.out.println("📋 All Headers:");
        java.util.Collections.list(request.getHeaderNames()).forEach(headerName -> {
            System.out.println("   " + headerName + ": " + request.getHeader(headerName));
        });

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No Bearer token found or invalid format");
            if (authHeader == null) {
                System.out.println("💥 Authorization header is completely missing");
            } else {
                System.out.println("💥 Authorization header exists but doesn't start with 'Bearer '");
                System.out.println("💥 Header value: '" + authHeader + "'");
            }
            System.out.println("➡️  Continuing filter chain without authentication");
            filterChain.doFilter(request, response);
            System.out.println("🏁 ========== JWT FILTER END (NO TOKEN) ==========");
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            System.out.println("✂️  Extracted JWT token: " + (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt));
            System.out.println("📏 Token length: " + jwt.length() + " characters");

            final String username = jwtUtil.extractUsername(jwt);
            System.out.println("👤 Extracted username from JWT: " + username);

            if (username == null) {
                System.out.println("💥 Username is null - token might be invalid or expired");
            } else {
                System.out.println("✅ Successfully extracted username: " + username);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("🔍 No existing authentication in SecurityContext - loading user details");

                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("✅ Loaded user details for: " + username);
                    System.out.println("👮 User authorities: " + userDetails.getAuthorities());

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        System.out.println("✅ JWT token validated successfully for user: " + username);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("🎉 User authenticated successfully: " + username);
                        System.out.println("🔐 Authentication set in SecurityContext: " +
                                SecurityContextHolder.getContext().getAuthentication());

                    } else {
                        System.out.println("❌ JWT token validation FAILED for user: " + username);
                        System.out.println("💡 Token might be expired or signature invalid");
                    }
                } catch (Exception e) {
                    System.out.println("❌ User not found in database: " + username);
                    System.out.println("💥 Exception: " + e.getMessage());
                }
            } else {
                if (username == null) {
                    System.out.println("💥 Username is null - cannot load user details");
                } else {
                    System.out.println("ℹ️  Authentication already exists in SecurityContext");
                    System.out.println("🔐 Current authentication: " +
                            SecurityContextHolder.getContext().getAuthentication());
                }
            }
        } catch (Exception e) {
            System.out.println("💥 ERROR processing JWT token: " + e.getMessage());
            System.out.println("🔄 Clearing SecurityContext due to error");
            e.printStackTrace();
            SecurityContextHolder.clearContext();
        }

        // Check final authentication state
        Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 Final SecurityContext authentication: " +
                (finalAuth != null ? finalAuth.getName() + " - " + finalAuth.getAuthorities() : "NULL"));

        System.out.println("➡️  Continuing to next filter/controller");
        filterChain.doFilter(request, response);

        System.out.println("🔚 ========== JWT FILTER END ==========");
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldNotFilter = path.startsWith("/api/auth/") ||
                path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/debug/") || // ADD THIS LINE
                path.equals("/") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/") ||
                path.equals("/error");

        System.out.println("🔍 Checking if should filter: " + path + " -> " + (shouldNotFilter ? "SKIP" : "PROCESS"));
        return shouldNotFilter;
    }
}