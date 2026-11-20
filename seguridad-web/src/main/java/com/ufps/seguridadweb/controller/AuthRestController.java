package com.ufps.seguridadweb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufps.seguridadweb.model.LoginRequest;
import com.ufps.seguridadweb.model.TokenResponse;
import com.ufps.seguridadweb.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthRestController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest peticion) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(peticion.username(), peticion.password()));
            UserDetails usuario = (UserDetails) autenticacion.getPrincipal();
            String token = jwtUtil.generarToken(usuario);
            return ResponseEntity.ok(new TokenResponse(token, "Bearer", jwtUtil.getExpiracionMs()));
        } catch (AuthenticationException e) {
            // Mensaje generico: no revela si fallo el usuario o la contrasena.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("estado", 401, "error", "Credenciales invalidas"));
        }
    }
}
