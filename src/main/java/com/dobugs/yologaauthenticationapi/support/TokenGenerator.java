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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenGenerator {

    private static final String PAYLOAD_NAME_OF_MEMBER_ID = "memberId";
    private static final String PAYLOAD_NAME_OF_PROVIDER = "provider";
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

    public ServiceTokenResponse create(final Long memberId, final String provider, final TokenResponse tokenResponse) {
        final Date now = new Date();
        final String accessToken = createToken(memberId, provider, tokenResponse.accessToken(), now, new Date(now.getTime() + tokenResponse.expiresIn() * 1000L));
        final String refreshToken = createToken(memberId, provider, tokenResponse.refreshToken(), now, extractRefreshTokenExpiration(tokenResponse, now));

        return new ServiceTokenResponse(accessToken, refreshToken);
    }

    public UserTokenResponse extract(final String serviceToken) {
        final String jwt = serviceToken.replace("Bearer ", "");
        final Claims claims = extractClaims(jwt);
        final Long memberId = extractMemberId(claims);
        final String token = extractToken(claims);
        final String provider = extractProvider(claims);
        return new UserTokenResponse(memberId, provider, token);
    }

    private Date extractRefreshTokenExpiration(final TokenResponse tokenResponse, final Date now) {
        final long expiresIn = tokenResponse.refreshTokenExpiresIn() * 1000L;
        if (expiresIn < 0) {
            return new Date(now.getTime() + defaultRefreshTokenExpiresIn);
        }
        return new Date(now.getTime() + expiresIn);
    }

    private String createToken(final Long memberId, final String provider, final String token, final Date issued, final Date expiration) {
        return Jwts.builder()
            .claim(PAYLOAD_NAME_OF_MEMBER_ID, memberId)
            .claim(PAYLOAD_NAME_OF_TOKEN, token)
            .claim(PAYLOAD_NAME_OF_PROVIDER, provider)
            .setIssuedAt(issued)
            .setExpiration(expiration)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    private Long extractMemberId(final Claims claims) {
        final Integer memberId = (Integer)claims.get(PAYLOAD_NAME_OF_MEMBER_ID);
        return memberId.longValue();
    }

    private String extractProvider(final Claims claims) {
        return (String) claims.get(PAYLOAD_NAME_OF_PROVIDER);
    }

    private String extractToken(final Claims claims) {
        return (String) claims.get(PAYLOAD_NAME_OF_TOKEN);
    }

    private Claims extractClaims(final String serviceToken) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(serviceToken)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("토큰의 만료 시간이 지났습니다.");
        }
    }
}
