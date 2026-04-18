package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void saveShouldPersistItem() {
        Item item = createTestItem();

        Item saved = itemRepository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void findAllShouldReturnAllItems() {
        itemRepository.save(createTestItem());
        itemRepository.save(createTestItem(2L, "Товар 2", 2000L));

        List<Item> items = itemRepository.findAll();

        assertThat(items).hasSize(2);
    }

    @Test
    void findByIdWhenExistsShouldReturnItem() {
        Item saved = itemRepository.save(createTestItem());

        Item found = itemRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void searchItemsShouldReturnFilteredItems() {
        itemRepository.save(createTestItem(1L, "Мяч футбольный", 1000L));
        itemRepository.save(createTestItem(2L, "Ракетка теннисная", 2000L));
        itemRepository.save(createTestItem(3L, "Велосипед", 3000L));

        Page<Item> result = itemRepository.searchItems("мяч", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Мяч");
    }

    @Test
    void searchItemsWithEmptySearchShouldReturnAllItems() {
        Item saved = itemRepository.save(createTestItem());

        Page<Item> result = itemRepository.searchItems("", PageRequest.of(0, 10));


        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(saved.getId());
    }

    private Item createTestItem() {
        return createTestItem(1L, "Тестовый товар", 1000L);
    }

    private Item createTestItem(Long id, String title, Long price) {
        return Item.builder()
                .title(title)
                .description("Описание " + title)
                .imgPath("/images/test.jpg")
                .price(price)
                .build();
    }
}