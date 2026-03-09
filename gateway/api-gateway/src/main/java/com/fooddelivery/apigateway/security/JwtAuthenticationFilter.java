// JwtAuthenticationFilter.java
package com.fooddelivery.apigateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // These must match exactly what SecurityConfig permits
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/h2-console/**",
            "/actuator/**",
            "/api/restaurants/search/**",
            "/hello/**",
            "/api/customers/hello-customer",
            "/api/orders/hello-customer",
            "/fallback/**"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Called by Spring Security
//    Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        return doFilter(exchange, chain::filter);
//    }

    // Called by Spring Cloud Gateway
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return doFilter(exchange, chain::filter);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange,
                                Function<ServerWebExchange, Mono<Void>> next) {
        String path = exchange.getRequest().getPath().toString();
        System.out.println("***** 123");
        if (isPublicPath(path)) {
            return next.apply(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("*** Bearer not around");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        System.out.println("*** Bearer around");

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            System.out.println("*** jwt not ok");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        System.out.println("*** jwt not ok");

        String username = jwtUtil.extractUsername(token);
        String role     = jwtUtil.extractRole(token);
        System.out.println("*** username = " + username + " role = " + role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-Authenticated-User-Id", username)
                .header("X-Authenticated-User-Role", role)
                .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                .build();

        return next.apply(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ReactiveSecurityContextHolder
                        .withAuthentication(authentication));
    }

    // Use AntPathMatcher so wildcards like /api/restaurants/*/menu work correctly
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // run before everything else
    }
}