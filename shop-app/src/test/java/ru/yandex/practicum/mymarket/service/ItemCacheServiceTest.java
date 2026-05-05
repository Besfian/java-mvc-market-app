package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemCacheServiceTest {

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private ReactiveRedisTemplate<String, Item> redisTemplate;

    @Test
    void testCacheAndRetrieveItem() {
        redisTemplate.keys("item:*")
                .flatMap(redisTemplate.opsForValue()::delete)
                .blockLast();

        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();

        StepVerifier.create(
                        itemCacheService.cacheItem(item)
                                .then(itemCacheService.getItem(1L))
                )
                .expectNextMatches(cachedItem ->
                        cachedItem.getId().equals(1L) &&
                                cachedItem.getTitle().equals("Тестовый товар") &&
                                cachedItem.getPrice().equals(1000L))
                .verifyComplete();
    }

    @Test
    void testCacheMiss() {
        StepVerifier.create(itemCacheService.getItem(999L))
                .verifyComplete(); // Должен вернуть пустой Mono
    }

    @Test
    void testEviction() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();

        StepVerifier.create(
                        itemCacheService.cacheItem(item)
                                .then(itemCacheService.evictItem(1L))
                                .then(itemCacheService.getItem(1L))
                )
                .verifyComplete();
    }
}