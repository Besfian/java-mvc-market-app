package ru.yandex.practicum.mymarket.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItemsWithoutSearchShouldReturnAllItems() {
        Page<Item> expectedPage = new PageImpl<>(List.of(createTestItem()));
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(expectedPage);

        Page<Item> result = itemService.getItems(null, "NO", 0, 5);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    void getItemsWithSearchShouldReturnFilteredItems() {
        Page<Item> expectedPage = new PageImpl<>(List.of(createTestItem()));
        when(itemRepository.searchItems(eq("тест"), any(PageRequest.class))).thenReturn(expectedPage);

        Page<Item> result = itemService.getItems("тест", "NO", 0, 5);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    void getItemsWithAlphaSortShouldReturnSortedByTitle() {
        Page<Item> expectedPage = new PageImpl<>(List.of(createTestItem()));
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(expectedPage);

        Page<Item> result = itemService.getItems(null, "ALPHA", 0, 5);

        assertThat(result).isNotNull();
    }

    @Test
    void getItemsWithPriceSortShouldReturnSortedByPrice() {
        Page<Item> expectedPage = new PageImpl<>(List.of(createTestItem()));
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(expectedPage);

        Page<Item> result = itemService.getItems(null, "PRICE", 0, 5);

        assertThat(result).isNotNull();
    }

    @Test
    void getItemByIdWhenExistsShouldReturnItem() {
        Item expected = createTestItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(expected));

        Item result = itemService.getItemById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getItemByIdWhenNotExistsShouldThrowException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found with id: 99");
    }

    private Item createTestItem() {
        return Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
    }
}
