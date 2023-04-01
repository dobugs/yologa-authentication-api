package com.dobugs.yologaauthenticationapi.auth;

import java.lang.annotation.Annotation;

import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.auth.exception.AuthorizationException;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenExtractor tokenExtractor;
    private final TokenRepository tokenRepository;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        if (!hasMethodAnnotation((HandlerMethod) handler, Authorized.class)) {
            return true;
        }
        final String jwt = extractAuthorizationHeader(request);
        final ServiceToken serviceToken = tokenExtractor.extract(jwt);
        final String savedRefreshToken = findRefreshToken(serviceToken);
        validateTheExistenceOfRefreshToken(serviceToken.token(), savedRefreshToken, handler);
        return true;
    }

    private <A extends Annotation> boolean hasMethodAnnotation(final HandlerMethod handler, final Class<A> annotationType) {
        return handler.hasMethodAnnotation(annotationType);
    }

    private String extractAuthorizationHeader(final HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization ==  null) {
            throw new AuthorizationException("토큰이 필요합니다.");
        }
        return authorization;
    }

    private String findRefreshToken(final ServiceToken serviceToken) {
        return tokenRepository.findRefreshToken(serviceToken.memberId())
            .orElseThrow(() -> new AuthorizationException("로그인이 필요한 서비스입니다."));
    }

    private void validateTheExistenceOfRefreshToken(final String refreshToken, final String savedRefreshToken, final Object handler) {
        if (hasMethodAnnotation((HandlerMethod) handler, ValidatedRefreshToken.class) && !savedRefreshToken.equals(refreshToken)) {
            throw new AuthorizationException("잘못된 refresh token 입니다.");
        }
    }
}
