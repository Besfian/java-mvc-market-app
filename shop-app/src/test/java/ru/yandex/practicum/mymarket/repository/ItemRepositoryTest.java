package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;

@DataR2dbcTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void saveShouldPersistItem() {
        Item item = createTestItem();

        Mono<Item> savedMono = itemRepository.save(item);

        StepVerifier.create(savedMono)
                .expectNextMatches(saved -> saved.getId() != null && "Тестовый товар".equals(saved.getTitle()))
                .verifyComplete();
    }

    @Test
    void findAllShouldReturnAllItems() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);
        Flux<Item> savedItems = itemRepository.saveAll(Flux.just(item1, item2));
        StepVerifier.create(savedItems.collectList())
                .expectNextMatches(list -> list.size() == 2)
                .verifyComplete();
    }

    @Test
    void findByIdWhenExistsShouldReturnItem() {
        Item item = createTestItem();
        Mono<Item> savedMono = itemRepository.save(item);
        StepVerifier.create(savedMono.flatMap(saved -> itemRepository.findById(saved.getId())))
                .expectNextMatches(found -> found.getTitle().equals("Тестовый товар"))
                .verifyComplete();
    }

    private Item createTestItem() {
        return createTestItem(1L, "Тестовый товар", 1000L);
    }

    private Item createTestItem(Long id, String title, Long price) {
        return Item.builder()
                .id(id)
                .title(title)
                .description("Описание " + title)
                .imgPath("/images/test.jpg")
                .price(price)
                .build();
    }
}