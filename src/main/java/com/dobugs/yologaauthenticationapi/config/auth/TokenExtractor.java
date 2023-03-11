package com.dobugs.yologaauthenticationapi.config.auth;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenExtractor {

    private static final String SERVICE_TOKEN_TYPE = "Bearer ";
    private static final String PAYLOAD_NAME_OF_MEMBER_ID = "memberId";
    private static final String PAYLOAD_NAME_OF_PROVIDER = "provider";
    private static final String PAYLOAD_NAME_OF_TOKEN_TYPE = "tokenType";
    private static final String PAYLOAD_NAME_OF_TOKEN = "token";

    private final SecretKey secretKey;

    public TokenExtractor(
        @Value("${jwt.token.secret-key}") final String secretKey
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));;
    }

    public ServiceToken extract(final String serviceToken) {
        final String jwt = serviceToken.replace(SERVICE_TOKEN_TYPE, "");
        final Claims claims = extractClaims(jwt);
        final Long memberId = extractMemberId(claims);
        final String provider = extractProvider(claims);
        final String tokenType = extractTokenType(claims);
        final String token = extractToken(claims);
        return new ServiceToken(memberId, provider, tokenType, token);
    }

    private Long extractMemberId(final Claims claims) {
        final Integer memberId = (Integer)claims.get(PAYLOAD_NAME_OF_MEMBER_ID);
        return memberId.longValue();
    }

    private String extractProvider(final Claims claims) {
        return (String) claims.get(PAYLOAD_NAME_OF_PROVIDER);
    }

    private String extractTokenType(final Claims claims) {
        return (String) claims.get(PAYLOAD_NAME_OF_TOKEN_TYPE);
    }

    private String extractToken(final Claims claims) {
        return (String) claims.get(PAYLOAD_NAME_OF_TOKEN);
    }

    private Claims extractClaims(final String serviceToken) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(serviceToken)
            .getBody();
    }
}
