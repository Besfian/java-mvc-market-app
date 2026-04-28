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

@Controller
@RequestMapping("/cart")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final CartItemEnricher cartItemEnricher;

    public CartController(CartService cartService, CartItemEnricher cartItemEnricher) {
        this.cartService = cartService;
        this.cartItemEnricher = cartItemEnricher;
    }

    @GetMapping("/items")
    public Mono<String> getCart(Model model, WebSession session) {
        log.info("GET /cart/items - Displaying cart for session: {}", session.getId());
        return cartItemEnricher.getCartItems(session)
                .collectList()
                .doOnNext(items -> log.debug("Cart has {} items", items.size()))
                .zipWith(cartItemEnricher.getTotalSum(session))
                .doOnNext(tuple -> {
                    log.info("Cart total: {} rub, items count: {}", tuple.getT2(), tuple.getT1().size());
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                })
                .thenReturn("cart");
    }

    @PostMapping("/items")
    public Mono<String> updateCart(
            @RequestParam Long id,
            @RequestParam String action,
            Model model,
            WebSession session) {
        log.info("POST /cart/items - id: {}, action: {}, session: {}", id, action, session.getId());
        Mono<?> cartAction;
        switch (action) {
            case "PLUS":
                log.debug("Increasing item {} in cart", id);
                cartAction = cartService.increaseItem(id, session);
                break;
            case "MINUS":
                log.debug("Decreasing item {} in cart", id);
                cartAction = cartService.decreaseItem(id, session);
                break;
            case "DELETE":
                log.debug("Removing item {} from cart", id);
                cartAction = cartService.removeItem(id, session);
                break;
            default:
                log.warn("Unknown action: {} for item {}", action, id);
                cartAction = Mono.empty();
        }

        return cartAction
                .doOnSuccess(v -> log.debug("Cart action completed for item {}", id))
                .then(cartItemEnricher.getCartItems(session).collectList())
                .zipWith(cartItemEnricher.getTotalSum(session))
                .doOnNext(tuple -> {
                    log.debug("Updated cart: {} items, total: {} rub", tuple.getT1().size(), tuple.getT2());
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                })
                .thenReturn("cart");
    }

    @PostMapping("/clear")
    public Mono<String> clearCart(WebSession session) {
        log.info("POST /cart/clear - Clearing cart for session: {}", session.getId());
        return cartService.clear(session)
                .thenReturn("redirect:/cart/items");
    }
}