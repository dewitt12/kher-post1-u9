package com.ufps.seguridadweb.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee "Authorization: Bearer <token>", valida firma y expiracion y puebla el SecurityContext.
 *
 * NO es un @Component a proposito: Spring Boot registraria automaticamente cualquier Filter-bean
 * en TODAS las peticiones (tambien las web de sesion). Se instancia solo dentro de apiFilterChain.
 *
 * Si el token falta o es invalido, el filtro no autentica nada; el AuthenticationEntryPoint
 * de la cadena responde 401.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            try {
                Claims claims = jwtUtil.validar(token);
                // Se recarga el usuario: si fue eliminado, el token deja de servir.
                UserDetails usuario = userDetailsService.loadUserByUsername(claims.getSubject());

                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token alterado, expirado, mal formado o de un usuario inexistente.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
