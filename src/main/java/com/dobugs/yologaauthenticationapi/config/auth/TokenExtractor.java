package com.dobugs.yologaauthenticationapi.config.auth;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.domain.JWTPayload;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenExtractor {

    private static final String SERVICE_TOKEN_TYPE = "Bearer ";

    private final SecretKey secretKey;

    public TokenExtractor(
        @Value("${jwt.token.secret-key}") final String secretKey
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));;
    }

    public ServiceToken extract(final String serviceToken) {
        final String jwt = serviceToken.replace(SERVICE_TOKEN_TYPE, "");
        final Claims claims = extractClaims(jwt);
        return new ServiceToken(
            extractMemberId(claims),
            extract(claims, JWTPayload.PROVIDER),
            extract(claims, JWTPayload.TOKEN_TYPE),
            extract(claims, JWTPayload.TOKEN)
        );
    }

    private Long extractMemberId(final Claims claims) {
        final Integer memberId = (Integer) claims.get(JWTPayload.MEMBER_ID.getName());
        return memberId.longValue();
    }

    private String extract(final Claims claims, final JWTPayload jwtPayload) {
        return (String) claims.get(jwtPayload.getName());
    }

    private Claims extractClaims(final String serviceToken) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(serviceToken)
            .getBody();
    }
}
