package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void testGetItemsShouldReturnItemsPage() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
        PageImpl<Item> page = new PageImpl<>(List.of(item), PageRequest.of(0, 5), 1);
        when(itemService.getItems(null, "NO", 0, 5)).thenReturn(Mono.just(page));
        when(cartService.getItemCount(eq(1L), any())).thenReturn(Mono.just(0));
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetItemsWithSearchAndSort() {
        PageImpl<Item> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(itemService.getItems("тест", "ALPHA", 0, 5)).thenReturn(Mono.just(page));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "тест")
                        .queryParam("sort", "ALPHA")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetItemShouldReturnItemPage() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));
        when(cartService.getItemCount(eq(1L), any())).thenReturn(Mono.just(2));
        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateCartFromItemsShouldRedirect() {
        when(cartService.increaseItem(eq(1L), any())).thenReturn(Mono.empty());
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .queryParam("search", "test")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "5")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=test&sort=NO&pageNumber=1&pageSize=5");
    }

    @Test
    void testUpdateCartFromItemShouldReturnItemPage() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .description("Описание")
                .imgPath("/images/test.jpg")
                .price(1000L)
                .build();
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));
        when(cartService.getItemCount(eq(1L), any())).thenReturn(Mono.just(1));
        when(cartService.increaseItem(eq(1L), any())).thenReturn(Mono.empty());
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }
}