package com.edusuite.platform.security;

import com.edusuite.platform.tenant.TenantIdentifierFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // stateless JWT API - re-enable if any session-cookie
                                           // based flows are added later (e.g. server-rendered
                                           // admin pages)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll() // trial signup, login redirect, etc.
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            // TenantIdentifierFilter MUST run after bearer-token authentication has populated
            // SecurityContextHolder, and before any controller logic executes. This ordering is
            // a security property (see class javadoc on TenantIdentifierFilter), not a style
            // choice - do not move this without re-reading that javadoc.
            .addFilterAfter(new TenantIdentifierFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
