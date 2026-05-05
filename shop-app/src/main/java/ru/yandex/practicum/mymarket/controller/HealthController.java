package ru.yandex.practicum.mymarket.controller;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public HealthController(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health/redis")
    public Mono<String> checkRedis() {
        return redisTemplate.opsForValue()
                .set("health-check", "ok")
                .then(redisTemplate.opsForValue().get("health-check"))
                .map(result -> "Redis is working: " + result)
                .onErrorReturn("Redis is not available");
    }
}