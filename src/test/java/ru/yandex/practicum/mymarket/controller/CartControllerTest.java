package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void testGetCartShouldReturnCartPage() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
        item.setCount(2);
        when(cartService.getCartItems(eq(itemService), any())).thenReturn(Flux.just(item));
        when(cartService.getTotalSum(eq(itemService), any())).thenReturn(Mono.just(2000L));
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateCartWithPlusActionShouldIncreaseItem() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(2);
        when(cartService.increaseItem(eq(1L), any())).thenReturn(Mono.empty());
        when(cartService.getCartItems(eq(itemService), any())).thenReturn(Flux.just(item));
        when(cartService.getTotalSum(eq(itemService), any())).thenReturn(Mono.just(2000L));
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateCartWithMinusActionShouldDecreaseItem() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(1);
        when(cartService.decreaseItem(eq(1L), any())).thenReturn(Mono.empty());
        when(cartService.getCartItems(eq(itemService), any())).thenReturn(Flux.just(item));
        when(cartService.getTotalSum(eq(itemService), any())).thenReturn(Mono.just(1000L));
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateCartWithDeleteActionShouldRemoveItem() {
        when(cartService.removeItem(eq(1L), any())).thenReturn(Mono.empty());
        when(cartService.getCartItems(eq(itemService), any())).thenReturn(Flux.empty());
        when(cartService.getTotalSum(eq(itemService), any())).thenReturn(Mono.just(0L));
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "DELETE")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }
}