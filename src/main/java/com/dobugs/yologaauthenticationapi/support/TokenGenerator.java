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
    private static final String PAYLOAD_NAME_OF_TOKEN_TYPE = "tokenType";
    private static final String PAYLOAD_NAME_OF_TOKEN = "token";

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

    public UserTokenResponse extract(final String serviceToken) {
        final String jwt = serviceToken.replace("Bearer ", "");
        final Claims claims = extractClaims(jwt);
        final Long memberId = extractMemberId(claims);
        final String provider = extractProvider(claims);
        final String tokenType = extractTokenType(claims);
        final String token = extractToken(claims);
        return new UserTokenResponse(memberId, provider, tokenType, token);
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
            .claim(PAYLOAD_NAME_OF_MEMBER_ID, memberId)
            .claim(PAYLOAD_NAME_OF_PROVIDER, provider)
            .claim(PAYLOAD_NAME_OF_TOKEN_TYPE, tokenType)
            .claim(PAYLOAD_NAME_OF_TOKEN, token)
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
