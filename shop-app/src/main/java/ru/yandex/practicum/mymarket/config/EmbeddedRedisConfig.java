package ru.yandex.practicum.mymarket.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "app.redis.embedded", havingValue = "true", matchIfMissing = true)
public class EmbeddedRedisConfig {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedRedisConfig.class);

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        log.info("Starting embedded Redis server...");
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
            log.info("Embedded Redis started successfully on port 6379");
        } catch (Exception e) {
            log.error("Failed to start embedded Redis: {}", e.getMessage());
            throw e;
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            log.info("Stopping embedded Redis server...");
            redisServer.stop();
            log.info("Embedded Redis stopped");
        }
    }
}