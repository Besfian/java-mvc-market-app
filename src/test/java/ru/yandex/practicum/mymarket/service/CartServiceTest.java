package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemService itemService;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartService cartService;

    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(sessionAttributes);
        when(session.getId()).thenReturn("test-session-id");
    }

    @Test
    void increaseItemShouldIncrementCount() {
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(1).verifyComplete();
    }

    @Test
    void increaseItemTwiceShouldIncreaseCountToTwo() {
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(2).verifyComplete();
    }

    @Test
    void decreaseItemWhenCountMoreThanOneShouldDecrementCount() {
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        .then(cartService.decreaseItem(1L, session))
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(1).verifyComplete();
    }

    @Test
    void decreaseItemWhenCountIsOneShouldRemoveItem() {
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.decreaseItem(1L, session))
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(0).verifyComplete();
    }

    @Test
    void removeItemShouldDeleteItem() {
        StepVerifier.create(
                        cartService.increaseItem(1L, session)
                                .then(cartService.increaseItem(2L, session))
                                .then(cartService.removeItem(1L, session))
                                .then(cartService.getItemCount(1L, session))
                                .zipWith(cartService.getItemCount(2L, session))
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
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        .then(cartService.increaseItem(2L, session))
                        .thenMany(cartService.getCartItems(itemService, session))
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
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        .then(cartService.increaseItem(2L, session))
                        .then(cartService.getTotalSum(itemService, session))
        ).expectNext(1000L * 2 + 2000L * 1).verifyComplete();
    }

    @Test
    void clearShouldRemoveAllItems() {
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(2L, session))
                        .then(cartService.clear(session))
                        .then(cartService.getCartItems(itemService, session).collectList())
        ).expectNextMatches(List::isEmpty).verifyComplete();
    }

    @Test
    void differentSessionsShouldHaveDifferentCarts() {
        // Создаем вторую сессию
        WebSession session2 = org.mockito.Mockito.mock(WebSession.class);
        Map<String, Object> sessionAttributes2 = new HashMap<>();
        when(session2.getAttributes()).thenReturn(sessionAttributes2);
        when(session2.getId()).thenReturn("test-session-id-2");

        // Добавляем товары в первую сессию
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        // Проверяем что во второй сессии корзина пустая
                        .then(cartService.getItemCount(1L, session2))
                        .doOnNext(count -> {
                            assert count == 0 : "Вторая сессия должна иметь пустую корзину";
                        })
                        // Добавляем другой товар во вторую сессию
                        .then(cartService.increaseItem(2L, session2))
                        // Проверяем что в первой сессии товар 2 отсутствует
                        .then(cartService.getItemCount(2L, session))
                        .doOnNext(count -> {
                            assert count == 0 : "Первая сессия не должна иметь товар 2";
                        })
                        // Проверяем что в первой сессии все еще есть товар 1
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(2).verifyComplete();
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