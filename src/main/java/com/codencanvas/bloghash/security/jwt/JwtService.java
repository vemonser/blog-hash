package com.codencanvas.bloghash.security.jwt;

import com.codencanvas.bloghash.config.properties.JwtProperties;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpiration;

    public JwtService(JwtProperties properties) {
        try {
            this.privateKey = loadPrivateKey(properties.privateKey().getInputStream().readAllBytes());
            this.publicKey = loadPublicKey(properties.publicKey().getInputStream().readAllBytes());
            this.accessTokenExpiration = properties.accessTokenExpiration();
            log.info("JWT keys loaded successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT RSA keys from PEM files. " +
                    "Run: openssl genrsa -out src/main/resources/keys/jwt-private.pem 2048 " +
                    "then: openssl rsa -in ... -pubout -out jwt-public.pem", e);
        }
    }

    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("username", principal.getUser().getUsername())
                .claim("role", principal.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(privateKey) // RS256 بيتحدد تلقائياً لأن privateKey هو RSAPrivateKey
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public Date extractExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public long getRemainingTtlMillis(String token) {
        Date expiration = extractExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private PrivateKey loadPrivateKey(byte[] pemBytes) throws Exception {
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        String stripped = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // remove all whitespace including \n

        byte[] decoded = Base64.getDecoder().decode(stripped);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(byte[] pemBytes) throws Exception {
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        String stripped = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(stripped);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

}
