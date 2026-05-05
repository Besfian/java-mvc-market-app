package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private CartService cartService;
    private WebSession session;

    @BeforeEach
    void setUp() {
        cartService = new CartService();
        session = mock(WebSession.class);

        // Создаём реальную мапу для хранения корзины
        Map<Long, Integer> cartMap = new HashMap<>();

        // Мокаем getAttribute для возврата нашей мапы
        when(session.getAttribute("cart")).thenReturn(cartMap);
        when(session.getId()).thenReturn("test-session-id");
    }

    @Test
    void increaseItemShouldIncrementCount() {
        cartService.increaseItem(1L, session).block();
        Integer count = cartService.getItemCount(1L, session).block();
        assertEquals(1, count);
    }

    @Test
    void increaseItemTwiceShouldIncreaseCountToTwo() {
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(1L, session).block();
        Integer count = cartService.getItemCount(1L, session).block();
        assertEquals(2, count);
    }

    @Test
    void decreaseItemWhenCountMoreThanOneShouldDecrementCount() {
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(1L, session).block();
        cartService.decreaseItem(1L, session).block();
        Integer count = cartService.getItemCount(1L, session).block();
        assertEquals(1, count);
    }

    @Test
    void decreaseItemWhenCountIsOneShouldRemoveItem() {
        cartService.increaseItem(1L, session).block();
        cartService.decreaseItem(1L, session).block();
        Integer count = cartService.getItemCount(1L, session).block();
        assertEquals(0, count);
    }

    @Test
    void removeItemShouldDeleteItem() {
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(2L, session).block();
        cartService.removeItem(1L, session).block();

        assertEquals(0, cartService.getItemCount(1L, session).block());
        assertEquals(1, cartService.getItemCount(2L, session).block());
    }

    @Test
    void clearShouldRemoveAllItems() {
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(2L, session).block();
        cartService.clear(session).block();

        assertEquals(0, cartService.getItemCount(1L, session).block());
        assertEquals(0, cartService.getItemCount(2L, session).block());
    }
}