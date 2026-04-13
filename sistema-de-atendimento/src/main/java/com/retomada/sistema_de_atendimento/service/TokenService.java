package com.retomada.sistema_de_atendimento.service;

import com.retomada.sistema_de_atendimento.model.Atendente;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.hours}")
    private long expirationHours;

    public String gerarToken(Atendente atendente) {
        Instant agora = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
        Instant expiracao = LocalDateTime.now().plusHours(expirationHours).toInstant(ZoneOffset.of("-03:00"));

        return Jwts.builder()
                .setIssuer("API Sistema de Atendimento")
                .setSubject(atendente.getUsername())
                .setIssuedAt(java.util.Date.from(agora))
                .setExpiration(java.util.Date.from(expiracao))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String tokenJWT) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(tokenJWT)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = this.secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}