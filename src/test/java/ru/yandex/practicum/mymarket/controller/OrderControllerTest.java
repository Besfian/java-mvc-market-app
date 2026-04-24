package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void testGetOrdersShouldReturnOrdersPage() {
        OrderItem orderItem = new OrderItem();
        orderItem.setTitle("Тестовый товар");
        orderItem.setPrice(1000L);
        orderItem.setCount(2);
        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(orderItem));
        when(orderService.getAllOrders()).thenReturn(Flux.just(order));
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetOrderShouldReturnOrderPage() {
        OrderItem orderItem = new OrderItem();
        orderItem.setTitle("Тестовый товар");
        orderItem.setPrice(1000L);
        orderItem.setCount(2);
        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(orderItem));
        when(orderService.getOrderById(1L)).thenReturn(Mono.just(order));
        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetOrderWithNewOrderFlag() {
        Order order = new Order();
        order.setId(1L);
        when(orderService.getOrderById(1L)).thenReturn(Mono.just(order));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/1")
                        .queryParam("newOrder", "true")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testBuyShouldCreateOrderAndRedirect() {
        Item item = Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
        item.setCount(2);
        Order order = new Order();
        order.setId(1L);
        when(cartService.getCartItems(itemService)).thenReturn(Flux.just(item));
        when(orderService.createOrder(anyList())).thenReturn(Mono.just(order));
        when(cartService.clear()).thenReturn(Mono.empty());
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");
    }

    @Test
    void testBuyEmptyCartShouldRedirectToCart() {
        when(cartService.getCartItems(itemService)).thenReturn(Flux.empty());
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");
    }
}