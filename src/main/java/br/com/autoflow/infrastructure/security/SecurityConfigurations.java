package br.com.autoflow.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurations {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MECANICO = "MECANICO";
    private static final String ROLE_CLIENTE = "CLIENTE";

    private static final String ORDENS_SERVICO_PATH = "/ordens-servico/**";
    private static final String VEICULOS_PATH = "/veiculos/**";
    private static final String ORCAMENTOS_PATH = "/orcamentos/**";
    private static final String SERVICOS_PATH = "/servicos/**";

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, ORDENS_SERVICO_PATH, VEICULOS_PATH, ORCAMENTOS_PATH, SERVICOS_PATH, "/os-servicos/**").hasAnyRole(ROLE_ADMIN, ROLE_MECANICO, ROLE_CLIENTE)
                        .requestMatchers(HttpMethod.POST, ORDENS_SERVICO_PATH, VEICULOS_PATH, ORCAMENTOS_PATH, SERVICOS_PATH).hasAnyRole(ROLE_ADMIN, ROLE_MECANICO)
                        .requestMatchers(HttpMethod.PUT, ORDENS_SERVICO_PATH, VEICULOS_PATH, ORCAMENTOS_PATH, SERVICOS_PATH).hasAnyRole(ROLE_ADMIN, ROLE_MECANICO)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}