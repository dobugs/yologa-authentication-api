package com.dobugs.yologaauthenticationapi.support.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Aspect
public class LoggingAspect {

    private final FileLogger fileLogger;
    private final SlackLogger slackLogger;

    @Pointcut("@annotation(com.dobugs.yologaauthenticationapi.support.logging.UnhandledExceptional)")
    private void unhandledException() {
    }

    @Around("unhandledException()")
    public Object log(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        printStackTrace(joinPoint);
        return result;
    }

    private void printStackTrace(final ProceedingJoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof final Exception exception) {
            final String stackTrace = getStackTrace(exception);
            System.out.println(stackTrace);
            fileLogger.write(stackTrace);
            slackLogger.alert(exception.getMessage(), stackTrace);
        }
    }

    private String getStackTrace(final Exception exception) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
