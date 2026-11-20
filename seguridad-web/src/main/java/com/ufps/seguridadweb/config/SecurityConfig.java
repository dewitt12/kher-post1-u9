package com.ufps.seguridadweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ufps.seguridadweb.security.JsonAccessDeniedHandler;
import com.ufps.seguridadweb.security.JsonAuthenticationEntryPoint;
import com.ufps.seguridadweb.security.JwtAuthFilter;
import com.ufps.seguridadweb.security.JwtUtil;

/**
 * Dos cadenas independientes que conviven sin interferirse:
 *  - apiFilterChain (@Order 1): solo /api/**, STATELESS, autentica con JWT.
 *  - webFilterChain (@Order 2): el resto de rutas, con SESION (form-login) y CSRF.
 * Spring evalua las cadenas por orden y usa la primera cuyo securityMatcher coincide.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                              JwtUtil jwtUtil,
                                              UserDetailsService userDetailsService) throws Exception {
        http
            .securityMatcher("/api/**")
            // CSRF se desactiva solo aqui: no hay cookie de sesion que un sitio externo pueda reutilizar,
            // el token viaja en el header Authorization y el navegador no lo adjunta por su cuenta.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtUtil, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                .accessDeniedHandler(new JsonAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }

    /** Lo usa el endpoint POST /api/auth/login para validar usuario y contrasena (BCrypt + BD). */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }
}
