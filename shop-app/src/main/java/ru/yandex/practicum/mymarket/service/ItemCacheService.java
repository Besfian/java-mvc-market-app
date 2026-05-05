package ru.yandex.practicum.mymarket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ItemCacheService {
    private static final Logger log = LoggerFactory.getLogger(ItemCacheService.class);
    private static final String ITEM_CACHE_PREFIX = "item:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, Item> redisTemplate;
    private final Map<Long, Item> localCache = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;

    public ItemCacheService(ReactiveRedisTemplate<String, Item> redisTemplate) {
        this.redisTemplate = redisTemplate;
        checkRedisAvailability();
    }

    private void checkRedisAvailability() {
        try {
            redisTemplate.opsForValue()
                    .get("health-check")
                    .timeout(Duration.ofSeconds(3))
                    .doOnSuccess(v -> {
                        redisAvailable = true;
                        log.info("Redis is available for caching");
                    })
                    .onErrorResume(e -> {
                        redisAvailable = false;
                        log.warn("Redis is not available, using in-memory cache. Error: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Failed to check Redis availability: {}", e.getMessage());
        }
    }

    public Mono<Item> getItem(Long id) {
        if (redisAvailable) {
            return getFromRedis(id)
                    .onErrorResume(e -> {
                        log.warn("Redis get failed for item {}, falling back to local cache", id, e);
                        return getFromLocalCache(id);
                    });
        } else {
            return getFromLocalCache(id);
        }
    }

    public Mono<Item> cacheItem(Item item) {
        if (redisAvailable) {
            return cacheInRedis(item)
                    .onErrorResume(e -> {
                        log.warn("Redis cache failed for item {}, using local cache", item.getId(), e);
                        redisAvailable = false;
                        return cacheInLocal(item);
                    });
        } else {
            return cacheInLocal(item);
        }
    }

    public Mono<Boolean> evictItem(Long id) {
        if (redisAvailable) {
            return evictFromRedis(id)
                    .onErrorResume(e -> {
                        log.warn("Redis evict failed for item {}", id, e);
                        return evictFromLocalCache(id);
                    });
        } else {
            return evictFromLocalCache(id);
        }
    }

    public Mono<Boolean> evictAll() {
        if (redisAvailable) {
            return evictAllFromRedis()
                    .onErrorResume(e -> {
                        log.warn("Redis evict all failed", e);
                        return evictAllFromLocalCache();
                    });
        } else {
            return evictAllFromLocalCache();
        }
    }

    private Mono<Item> getFromRedis(Long id) {
        String key = ITEM_CACHE_PREFIX + id;
        return redisTemplate.opsForValue()
                .get(key)
                .doOnNext(item -> log.debug("Redis cache hit for item: {}", id))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Redis cache miss for item: {}", id);
                    return Mono.empty();
                }));
    }

    private Mono<Item> cacheInRedis(Item item) {
        String key = ITEM_CACHE_PREFIX + item.getId();
        return redisTemplate.opsForValue()
                .set(key, item, CACHE_TTL)
                .doOnSuccess(v -> log.debug("Cached item in Redis: {}", item.getId()))
                .thenReturn(item);
    }

    private Mono<Boolean> evictFromRedis(Long id) {
        String key = ITEM_CACHE_PREFIX + id;
        return redisTemplate.opsForValue()
                .delete(key)
                .doOnNext(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        log.debug("Evicted item from Redis cache: {}", id);
                    }
                });
    }

    private Mono<Boolean> evictAllFromRedis() {
        return redisTemplate.keys(ITEM_CACHE_PREFIX + "*")
                .flatMap(redisTemplate.opsForValue()::delete)
                .collectList()
                .map(results -> results.stream().reduce(true, (a, b) -> a && b))
                .doOnNext(result -> log.info("Evicted all items from Redis cache"));
    }

    private Mono<Item> getFromLocalCache(Long id) {
        Item item = localCache.get(id);
        if (item != null) {
            log.debug("Local cache hit for item: {}", id);
            return Mono.just(item);
        }
        log.debug("Local cache miss for item: {}", id);
        return Mono.empty();
    }

    private Mono<Item> cacheInLocal(Item item) {
        localCache.put(item.getId(), item);
        log.debug("Cached item in local cache: {}", item.getId());
        return Mono.just(item);
    }

    private Mono<Boolean> evictFromLocalCache(Long id) {
        Item removed = localCache.remove(id);
        boolean result = removed != null;
        if (result) {
            log.debug("Evicted item from local cache: {}", id);
        }
        return Mono.just(result);
    }

    private Mono<Boolean> evictAllFromLocalCache() {
        localCache.clear();
        log.info("Evicted all items from local cache");
        return Mono.just(true);
    }
}