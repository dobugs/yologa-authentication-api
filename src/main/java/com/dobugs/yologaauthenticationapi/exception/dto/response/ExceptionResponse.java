package com.dobugs.yologaauthenticationapi.exception.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ExceptionResponse {

    private final String message;

    public static ExceptionResponse from(final String message) {
        return new ExceptionResponse(message);
    }

    public static ExceptionResponse from(final String customMessage, final String originMessage) {
        final String message = String.format("%s : %s", customMessage, originMessage);
        return new ExceptionResponse(message);
    }
}
