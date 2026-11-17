package com.ufps.seguridadweb.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad dedicada a emitir y validar JWT firmados con HMAC-SHA256.
 * La clave y la expiracion vienen de application.properties / variables de entorno.
 */
@Component
public class JwtUtil {

    private static final int MIN_BYTES_CLAVE = 32; // HS256 exige >= 256 bits

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secreto,
                   @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_BYTES_CLAVE) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(UserDetails usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica la firma y la expiracion. Lanza JwtException (ExpiredJwtException,
     * SignatureException, MalformedJwtException...) si el token no es valido.
     */
    public Claims validar(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}
