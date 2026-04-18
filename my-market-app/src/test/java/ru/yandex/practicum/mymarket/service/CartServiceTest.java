package ru.yandex.practicum.mymarket.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService.clear();
    }

    @Test
    void addItemShouldIncreaseCount() {
        Item item = createTestItem();

        cartService.addItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(1);
    }

    @Test
    void addItemTwiceShouldIncreaseCountToTwo() {
        cartService.addItem(1L);

        cartService.addItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(2);
    }

    @Test
    void increaseItemShouldIncrementCount() {
        cartService.addItem(1L);

        cartService.increaseItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(2);
    }

    @Test
    void decreaseItemWhenCountMoreThanOneShouldDecrementCount() {
        cartService.addItem(1L);
        cartService.addItem(1L);

        cartService.decreaseItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(1);
    }

    @Test
    void decreaseItemWhenCountIsOneShouldRemoveItem() {
        cartService.addItem(1L);

        cartService.decreaseItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(0);
    }

    @Test
    void removeItemShouldDeleteItem() {
        cartService.addItem(1L);
        cartService.addItem(2L);

        cartService.removeItem(1L);

        assertThat(cartService.getItemCount(1L)).isEqualTo(0);
        assertThat(cartService.getItemCount(2L)).isEqualTo(1);
    }

    @Test
    void getCartItemsShouldReturnItemsWithCounts() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);

        cartService.addItem(1L);
        cartService.addItem(1L);
        cartService.addItem(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        List<Item> items = cartService.getCartItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getCount()).isEqualTo(2);
        assertThat(items.get(1).getCount()).isEqualTo(1);
    }

    @Test
    void getTotalSumShouldCalculateCorrectly() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);

        cartService.addItem(1L);
        cartService.addItem(1L);
        cartService.addItem(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        long total = cartService.getTotalSum();

        assertThat(total).isEqualTo(1000L * 2 + 2000L * 1);
    }

    @Test
    void clearShouldRemoveAllItems() {
        cartService.addItem(1L);
        cartService.addItem(2L);

        cartService.clear();

        assertThat(cartService.getCartItems()).isEmpty();
        assertThat(cartService.getTotalSum()).isEqualTo(0);
    }

    private Item createTestItem() {
        return createTestItem(1L, "Тестовый товар", 1000L);
    }

    private Item createTestItem(Long id, String title, Long price) {
        return Item.builder()
                .id(id)
                .title(title)
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(price)
                .build();
    }
}
