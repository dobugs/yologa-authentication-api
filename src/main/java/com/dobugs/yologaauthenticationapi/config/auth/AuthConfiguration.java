package com.dobugs.yologaauthenticationapi.config.auth;

import org.springframework.context.annotation.Configuration;
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
}
