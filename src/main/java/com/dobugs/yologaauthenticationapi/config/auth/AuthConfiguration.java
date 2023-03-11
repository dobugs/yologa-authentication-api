package com.dobugs.yologaauthenticationapi.config.auth;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dobugs.yologaauthenticationapi.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class AuthConfiguration implements WebMvcConfigurer {

    private final TokenExtractor tokenExtractor;
    private final TokenRepository tokenRepository;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(tokenExtractor, tokenRepository))
            .addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TokenResolver(tokenExtractor));
    }
}
