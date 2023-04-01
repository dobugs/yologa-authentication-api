package com.dobugs.yologaauthenticationapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.dobugs.yologaauthenticationapi.support.FileGenerator;
import com.dobugs.yologaauthenticationapi.support.logging.FileLogger;
import com.dobugs.yologaauthenticationapi.support.logging.LoggingAspect;
import com.dobugs.yologaauthenticationapi.support.logging.SlackLogger;

@EnableAspectJAutoProxy
@Configuration
public class AspectConfiguration {

    @Value("${logging.file-path}")
    private String savedDirectory;

    @Value("${slack.url}")
    private String slackUrl;

    @Value("${slack.token}")
    private String slackToken;

    @Value("${slack.channel-id}")
    private String slackChannelId;

    @Bean
    public FileLogger fileLogger() {
        return new FileLogger(FileGenerator.getInstance(), savedDirectory);
    }

    @Bean
    public SlackLogger slackLogger() {
        return new SlackLogger(slackUrl, slackToken, slackChannelId);
    }

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect(fileLogger(), slackLogger());
    }
}
