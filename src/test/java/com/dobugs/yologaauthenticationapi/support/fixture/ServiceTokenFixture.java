package com.dobugs.yologaauthenticationapi.support.fixture;

import java.util.Date;

import javax.crypto.SecretKey;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class ServiceTokenFixture {

    public static Integer extractMemberId(final String createdToken, final SecretKey secretKey) {
        return (Integer) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(createdToken)
            .getBody()
            .get("memberId");
    }

    public static String extractToken(final String createdToken, final SecretKey secretKey) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(createdToken)
            .getBody()
            .get("token");
    }

    public static String extractProvider(final String createdToken, final SecretKey secretKey) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(createdToken)
            .getBody()
            .get("provider");
    }

    public static String extractTokenType(final String createdToken, final SecretKey secretKey) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(createdToken)
            .getBody()
            .get("tokenType");
    }

    public static class Builder {

        private Long memberId;
        private String provider;
        private String tokenType;
        private String token;
        private Date expiration;
        private SecretKey secretKey;
        private SignatureAlgorithm algorithm;

        public Builder() {
        }

        public String compact() {
            final JwtBuilder jwtBuilder = Jwts.builder();
            if (memberId != null) {
                jwtBuilder.claim("memberId", memberId);
            }
            if (provider != null) {
                jwtBuilder.claim("provider", provider);
            }
            if (tokenType != null) {
                jwtBuilder.claim("tokenType", tokenType);
            }
            if (token != null) {
                jwtBuilder.claim("token", token);
            }
            if (expiration != null) {
                jwtBuilder.setExpiration(expiration);
            }
            if (secretKey != null && algorithm != null) {
                jwtBuilder.signWith(secretKey, algorithm);
            }
            return jwtBuilder.compact();
        }

        public ServiceToken build() {
            return new ServiceToken(memberId, provider, tokenType, token);
        }

        public Builder memberId(final Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder provider(final String provider) {
            this.provider = provider;
            return this;
        }

        public Builder tokenType(final String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder token(final String token) {
            this.token = token;
            return this;
        }

        public Builder expiration(final Date expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder secretKey(final SecretKey secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder algorithm(final SignatureAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }
    }
}
