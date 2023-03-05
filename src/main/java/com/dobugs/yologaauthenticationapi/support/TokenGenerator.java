package com.dobugs.yologaauthenticationapi.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Claims;
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

    public ServiceTokenDto create(final Long memberId, final String provider, final OAuthTokenDto oAuthTokenDto) {
        final Date now = new Date();
        final String accessToken = createToken(memberId, provider, oAuthTokenDto.accessToken(), now, new Date(now.getTime() + oAuthTokenDto.expiresIn() * 1000L));
        final String refreshToken = createToken(memberId, provider, oAuthTokenDto.refreshToken(), now, new Date(now.getTime() + oAuthTokenDto.refreshTokenExpiresIn() * 1000L));

        return new ServiceTokenDto(accessToken, refreshToken);
    }

    public UserTokenResponse extract(final String serviceToken) {
        final String jwt = serviceToken.replace("Bearer ", "");
        final Claims claims = extractClaims(jwt);
        final Long memberId = extractMemberId(claims);
        final String token = extractToken(claims);
        final String provider = extractProvider(claims);
        return new UserTokenResponse(memberId, provider, token);
    }

    public OAuthTokenDto setUpRefreshTokenExpiration(final OAuthTokenResponse oAuthTokenResponse) {
        int expiresIn = oAuthTokenResponse.refreshTokenExpiresIn();
        if (expiresIn < 0) {
            expiresIn = defaultRefreshTokenExpiresIn;
        }
        return new OAuthTokenDto(
            oAuthTokenResponse.accessToken(), oAuthTokenResponse.expiresIn(),
            oAuthTokenResponse.refreshToken(), expiresIn,
            oAuthTokenResponse.tokenType()
        );
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
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(serviceToken)
            .getBody();
    }
}
