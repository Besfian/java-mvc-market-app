package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService.clear().block();
    }

    @Test
    void increaseItemShouldIncrementCount() {
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.getItemCount(1L))
        ).expectNext(1).verifyComplete();
    }

    @Test
    void increaseItemTwiceShouldIncreaseCountToTwo() {
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.increaseItem(1L))
                        .then(cartService.getItemCount(1L))
        ).expectNext(2).verifyComplete();
    }

    @Test
    void decreaseItemWhenCountMoreThanOneShouldDecrementCount() {
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.increaseItem(1L))
                        .then(cartService.decreaseItem(1L))
                        .then(cartService.getItemCount(1L))
        ).expectNext(1).verifyComplete();
    }

    @Test
    void decreaseItemWhenCountIsOneShouldRemoveItem() {
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.decreaseItem(1L))
                        .then(cartService.getItemCount(1L))
        ).expectNext(0).verifyComplete();
    }

    @Test
    void removeItemShouldDeleteItem() {
        StepVerifier.create(
                        cartService.increaseItem(1L)
                                .then(cartService.increaseItem(2L))
                                .then(cartService.removeItem(1L))
                                .then(cartService.getItemCount(1L))
                                .zipWith(cartService.getItemCount(2L))
                ).expectNextMatches(tuple -> tuple.getT1() == 0 && tuple.getT2() == 1)
                .verifyComplete();
    }

    @Test
    void getCartItemsShouldReturnItemsWithCounts() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item1));
        when(itemService.getItemById(2L)).thenReturn(Mono.just(item2));
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.increaseItem(1L))
                        .then(cartService.increaseItem(2L))
                        .thenMany(cartService.getCartItems(itemService))
                        .collectList()
        ).expectNextMatches(items ->
                items.size() == 2 &&
                        items.stream().filter(i -> i.getId() == 1L).findFirst().get().getCount() == 2 &&
                        items.stream().filter(i -> i.getId() == 2L).findFirst().get().getCount() == 1
        ).verifyComplete();
    }

    @Test
    void getTotalSumShouldCalculateCorrectly() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item1));
        when(itemService.getItemById(2L)).thenReturn(Mono.just(item2));
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.increaseItem(1L))
                        .then(cartService.increaseItem(2L))
                        .then(cartService.getTotalSum(itemService))
        ).expectNext(1000L * 2 + 2000L * 1).verifyComplete();
    }

    @Test
    void clearShouldRemoveAllItems() {
        StepVerifier.create(
                cartService.increaseItem(1L)
                        .then(cartService.increaseItem(2L))
                        .then(cartService.clear())
                        .then(cartService.getCartItems(itemService).collectList())
        ).expectNextMatches(List::isEmpty).verifyComplete();
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