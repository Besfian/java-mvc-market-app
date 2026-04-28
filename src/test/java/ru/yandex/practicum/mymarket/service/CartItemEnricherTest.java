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
class CartItemEnricherTest {

    @Mock
    private ItemService itemService;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartItemEnricher cartItemEnricher;

    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(sessionAttributes);
        when(session.getId()).thenReturn("test-session-id");
        CartService cartService = new CartService();
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(1L, session).block();
        cartService.increaseItem(2L, session).block();
    }

    @Test
    void getCartItemsShouldReturnItemsWithCounts() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item1));
        when(itemService.getItemById(2L)).thenReturn(Mono.just(item2));
        StepVerifier.create(cartItemEnricher.getCartItems(session).collectList())
                .expectNextMatches(items ->
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
        StepVerifier.create(cartItemEnricher.getTotalSum(session))
                .expectNext(1000L * 2 + 2000L * 1)
                .verifyComplete();
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