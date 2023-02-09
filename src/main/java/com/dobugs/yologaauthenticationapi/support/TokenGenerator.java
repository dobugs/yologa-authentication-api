package com.dobugs.yologaauthenticationapi.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenGenerator {

    private static final String PAYLOAD_NAME_OF_MEMBER_ID = "memberId";
    private static final String PAYLOAD_NAME_OF_TOKEN = "token";

    private final SecretKey secretKey;
    private final int defaultRefreshTokenExpiresIn;

    public TokenGenerator(
        @Value("${jwt.token.secret-key}") final String secretKey,
        @Value("${jwt.token.refresh-token.default-expires-in}") final int defaultRefreshTokenExpiresIn
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));;
        this.defaultRefreshTokenExpiresIn = defaultRefreshTokenExpiresIn;
    }

    public ServiceTokenResponse create(final Long memberId, final TokenResponse tokenResponse) {
        final Date now = new Date();
        final String accessToken = createToken(memberId, tokenResponse.accessToken(), now, new Date(now.getTime() + tokenResponse.expiresIn()));
        final String refreshToken = createToken(memberId, tokenResponse.refreshToken(), now, extractRefreshTokenExpiration(tokenResponse, now));

        return new ServiceTokenResponse(accessToken, refreshToken);
    }

    public UserTokenResponse extract(final String serviceToken) {
        final Long memberId = extractMemberId(serviceToken);
        final String token = extractToken(serviceToken);
        return new UserTokenResponse(memberId, token);
    }

    private Date extractRefreshTokenExpiration(final TokenResponse tokenResponse, final Date now) {
        final int expiresIn = tokenResponse.refreshTokenExpiresIn();
        if (expiresIn < 0) {
            return new Date(now.getTime() + defaultRefreshTokenExpiresIn);
        }
        return new Date(now.getTime() + expiresIn);
    }

    private String createToken(final Long memberId, final String token, final Date issued, final Date expiration) {
        return Jwts.builder()
            .claim(PAYLOAD_NAME_OF_MEMBER_ID, memberId)
            .claim(PAYLOAD_NAME_OF_TOKEN, token)
            .setIssuedAt(issued)
            .setExpiration(expiration)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    private Long extractMemberId(final String serviceToken) {
        return (Long) extractClaims(serviceToken)
            .get(PAYLOAD_NAME_OF_MEMBER_ID);
    }

    private String extractToken(final String serviceToken) {
        return (String) extractClaims(serviceToken)
            .get(PAYLOAD_NAME_OF_TOKEN);
    }

    private Claims extractClaims(final String serviceToken) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(serviceToken)
            .getBody();
    }
}
