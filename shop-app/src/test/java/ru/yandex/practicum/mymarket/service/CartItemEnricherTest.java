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
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartItemEnricherTest {

    @Mock
    private ItemService itemService;

    @Mock
    private ItemCacheService itemCacheService;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartItemEnricher cartItemEnricher;

    private Map<Long, Integer> cartMap;

    @BeforeEach
    void setUp() {
        cartMap = new HashMap<>();
        cartMap.put(1L, 2);  // Товар 1: количество 2
        cartMap.put(2L, 1);  // Товар 2: количество 1

        // ВАЖНО: мокаем session.getAttribute("cart")
        when(session.getAttribute("cart")).thenReturn(cartMap);
        when(session.getId()).thenReturn("test-session-id");
    }

    @Test
    void getCartItemsShouldReturnItemsWithCounts() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);

        when(itemCacheService.getItem(1L)).thenReturn(Mono.empty());
        when(itemCacheService.getItem(2L)).thenReturn(Mono.empty());
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item1));
        when(itemService.getItemById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(cartItemEnricher.getCartItems(session).collectList())
                .expectNextMatches(items -> {
                    if (items.size() != 2) return false;
                    Item first = items.stream().filter(i -> i.getId() == 1L).findFirst().orElse(null);
                    Item second = items.stream().filter(i -> i.getId() == 2L).findFirst().orElse(null);
                    return first != null && first.getCount() == 2 &&
                            second != null && second.getCount() == 1;
                })
                .verifyComplete();
    }

    @Test
    void getTotalSumShouldCalculateCorrectly() {
        Item item1 = createTestItem(1L, "Товар 1", 1000L);
        Item item2 = createTestItem(2L, "Товар 2", 2000L);

        when(itemCacheService.getItem(1L)).thenReturn(Mono.empty());
        when(itemCacheService.getItem(2L)).thenReturn(Mono.empty());
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item1));
        when(itemService.getItemById(2L)).thenReturn(Mono.just(item2));

        // 1000*2 + 2000*1 = 4000
        StepVerifier.create(cartItemEnricher.getTotalSum(session))
                .expectNext(4000L)
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