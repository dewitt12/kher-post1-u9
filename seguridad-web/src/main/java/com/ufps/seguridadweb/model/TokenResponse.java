package com.ufps.seguridadweb.model;

public record TokenResponse(String token, String tipo, long expiraEnMs) {
}
