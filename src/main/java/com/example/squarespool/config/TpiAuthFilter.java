package com.example.squarespool.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class TpiAuthFilter extends OncePerRequestFilter {
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/",
            "/login",
            "/admin",
            "/admin-login",
            "/api/auth",
            "/swagger-ui.html",
            "/tpi/rest/swagger-ui/index.html",
            "/v3/api-docs",
            "/ws"
    );

    private final AppProperties appProperties;

    public TpiAuthFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if (!appProperties.isAuthEnabled() || shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(appProperties.getAuthUserHeader());
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing auth header\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String prefix : SKIP_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        if (path.startsWith("/boards/") && path.endsWith("/view")) {
            return true;
        }
        return !path.startsWith("/boards") && !path.startsWith("/admin");
    }
}
