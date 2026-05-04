package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ItemServiceWithCacheTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Item> redisTemplate;

    private Item testItem;

    @BeforeEach
    void setUp() {
        System.setProperty("spring.redis.host", redis.getHost());
        System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());

        redisTemplate.keys("item:*")
                .flatMap(redisTemplate.opsForValue()::delete)
                .blockLast();

        testItem = Item.builder()
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
    }

    @Test
    void testFirstRequestHitsDatabaseSecondFromCache() {
        Item savedItem = itemRepository.save(testItem).block();

        StepVerifier.create(itemService.getItemById(savedItem.getId())
                        .doOnNext(item -> {
                            assertThat(item.getTitle()).isEqualTo("Тестовый товар");
                            Item cachedItem = itemCacheService.getItem(item.getId()).block();
                            assertThat(cachedItem).isNotNull();
                        }))
                .expectNextCount(1)
                .verifyComplete();

        itemRepository.deleteById(savedItem.getId()).block();

        StepVerifier.create(itemService.getItemById(savedItem.getId()))
                .expectNextCount(1)
                .verifyComplete();
    }
}