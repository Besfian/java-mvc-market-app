package ru.yandex.practicum.mymarket.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.CartItemEnricher;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final CartService cartService;
    private final CartItemEnricher cartItemEnricher;

    public OrderController(OrderService orderService, CartService cartService, CartItemEnricher cartItemEnricher) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.cartItemEnricher = cartItemEnricher;
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        log.info("GET /orders - Fetching all orders");
        return orderService.getAllOrders()
                .collectList()
                .doOnNext(orders -> log.info("Found {} orders", orders.size()))
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {
        log.info("GET /orders/{} - newOrder: {}", id, newOrder);
        return orderService.getOrderById(id)
                .doOnNext(order -> log.debug("Found order {} with {} items", id, order.getItems().size()))
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        log.info("POST /buy - Processing purchase for session: {}", session.getId());
        return cartItemEnricher.getCartItems(session)
                .collectList()
                .doOnNext(cartItems -> log.info("Cart has {} items for purchase", cartItems.size()))
                .filter(cartItems -> {
                    if (cartItems.isEmpty()) {
                        log.warn("Attempted to buy with empty cart");
                        return false;
                    }
                    return true;
                })
                .flatMap(orderService::createOrder)
                .doOnNext(order -> log.info("Order created successfully: {}", order.getId()))
                .flatMap(order -> cartService.clear(session)
                        .doOnSuccess(v -> log.info("Cart cleared after order {}", order.getId()))
                        .thenReturn(order))
                .map(order -> {
                    String redirectUrl = "redirect:/orders/" + order.getId() + "?newOrder=true";
                    log.info("Redirecting to: {}", redirectUrl);
                    return redirectUrl;
                })
                .defaultIfEmpty("redirect:/cart/items")
                .doOnSuccess(url -> {
                    if (url.equals("redirect:/cart/items")) {
                        log.warn("Redirecting to empty cart because purchase failed or cart was empty");
                    }
                });
    }
}