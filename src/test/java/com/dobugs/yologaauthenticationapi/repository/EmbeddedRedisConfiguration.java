package com.dobugs.yologaauthenticationapi.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedisConfiguration {

    @Value("${spring.data.redis.port}")
    private int port;

    private RedisServer server;

    @PostConstruct
    public void start() {
        server = new RedisServer(port);
        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }
}
