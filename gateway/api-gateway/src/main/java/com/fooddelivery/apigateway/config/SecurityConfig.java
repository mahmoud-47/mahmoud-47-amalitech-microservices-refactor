// SecurityConfig.java
package com.fooddelivery.apigateway.config;

import com.fooddelivery.apigateway.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers("/api/orders/hello-customer").permitAll()
//
//
//
//                        .pathMatchers("/api/auth/**").permitAll()
//                        .pathMatchers("/h2-console/**").permitAll()
//                        .pathMatchers("/actuator/**").permitAll()
//                        .pathMatchers("/api/restaurants/search/**").permitAll()
//                        .pathMatchers("/api/restaurants/*/menu").permitAll()
//                        .pathMatchers("/hello/**").permitAll()
                        // All other requests must have authentication set by JwtAuthenticationFilter
                        .anyExchange().permitAll()
                )
                // Run JWT filter before Spring Security's own authentication step
                .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}