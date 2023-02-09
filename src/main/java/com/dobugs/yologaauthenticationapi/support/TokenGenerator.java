package com.dobugs.yologaauthenticationapi.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenGenerator {

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

    private Date extractRefreshTokenExpiration(final TokenResponse tokenResponse, final Date now) {
        final int expiresIn = tokenResponse.refreshTokenExpiresIn();
        if (expiresIn < 0) {
            return new Date(now.getTime() + defaultRefreshTokenExpiresIn);
        }
        return new Date(now.getTime() + expiresIn);
    }

    private String createToken(final Long memberId, final String token, final Date issued, final Date expiration) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .claim("token", token)
            .setIssuedAt(issued)
            .setExpiration(expiration)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
}
