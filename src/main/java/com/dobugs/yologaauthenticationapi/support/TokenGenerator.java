package com.dobugs.yologaauthenticationapi.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.domain.JWTPayload;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenDto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenGenerator {

    private final SecretKey secretKey;
    private final long accessTokenExpiresIn;
    private final long refreshTokenExpiresIn;

    public TokenGenerator(
        @Value("${jwt.token.secret-key}") final String secretKey,
        @Value("${jwt.token.access-token.default-expires-in}") final long accessTokenExpiresIn,
        @Value("${jwt.token.refresh-token.default-expires-in}") final long refreshTokenExpiresIn
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public ServiceTokenDto create(final Long memberId, final String provider, final OAuthTokenDto oAuthTokenDto) {
        final String accessToken = createToken(memberId, provider, oAuthTokenDto.tokenType(), oAuthTokenDto.accessToken(), oAuthTokenDto.expiresIn());
        final String refreshToken = createToken(memberId, provider, oAuthTokenDto.tokenType(), oAuthTokenDto.refreshToken(), oAuthTokenDto.refreshTokenExpiresIn());
        return new ServiceTokenDto(accessToken, refreshToken);
    }

    public OAuthTokenDto setUpExpiration(final OAuthTokenResponse oAuthTokenResponse) {
        return new OAuthTokenDto(
            oAuthTokenResponse.accessToken(), accessTokenExpiresIn,
            oAuthTokenResponse.refreshToken(), refreshTokenExpiresIn,
            oAuthTokenResponse.tokenType()
        );
    }

    private String createToken(final Long memberId, final String provider, final String tokenType, final String token, final long expiresIn) {
        final Date issued = new Date();
        final Date expiration = new Date(issued.getTime() + expiresIn);
        return Jwts.builder()
            .claim(JWTPayload.MEMBER_ID.getName(), memberId)
            .claim(JWTPayload.PROVIDER.getName(), provider)
            .claim(JWTPayload.TOKEN_TYPE.getName(), tokenType)
            .claim(JWTPayload.TOKEN.getName(), token)
            .setIssuedAt(issued)
            .setExpiration(expiration)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
}
