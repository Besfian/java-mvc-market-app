package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

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
    void clearShouldRemoveAllItems() {
        StepVerifier.create(
                        cartService.increaseItem(1L, session)
                                .then(cartService.increaseItem(2L, session))
                                .then(cartService.clear(session))
                                .then(cartService.getItemCount(1L, session))
                                .zipWith(cartService.getItemCount(2L, session))
                ).expectNextMatches(tuple -> tuple.getT1() == 0 && tuple.getT2() == 0)
                .verifyComplete();
    }

    @Test
    void differentSessionsShouldHaveDifferentCarts() {
        WebSession session2 = org.mockito.Mockito.mock(WebSession.class);
        Map<String, Object> sessionAttributes2 = new HashMap<>();
        when(session2.getAttributes()).thenReturn(sessionAttributes2);
        when(session2.getId()).thenReturn("test-session-id-2");
        StepVerifier.create(
                cartService.increaseItem(1L, session)
                        .then(cartService.increaseItem(1L, session))
                        .then(cartService.getItemCount(1L, session2))
                        .doOnNext(count -> {
                            assert count == 0 : "Вторая сессия должна иметь пустую корзину";
                        })
                        .then(cartService.increaseItem(2L, session2))
                        .then(cartService.getItemCount(2L, session))
                        .doOnNext(count -> {
                            assert count == 0 : "Первая сессия не должна иметь товар 2";
                        })
                        .then(cartService.getItemCount(1L, session))
        ).expectNext(2).verifyComplete();
    }
}