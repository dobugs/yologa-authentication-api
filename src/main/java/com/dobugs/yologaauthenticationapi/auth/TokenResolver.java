package com.dobugs.yologaauthenticationapi.auth;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.dobugs.yologaauthenticationapi.auth.exception.AuthorizationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenResolver implements HandlerMethodArgumentResolver {

    private final TokenExtractor tokenExtractor;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ExtractAuthorization.class);
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter, final ModelAndViewContainer mavContainer,
        final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
        return tokenExtractor.extract(extractAuthorizationHeader(request));
    }

    private String extractAuthorizationHeader(final HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization ==  null) {
            throw new AuthorizationException("토큰이 필요합니다.");
        }
        return decode(authorization);
    }

    private String decode(final String encoded) {
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
