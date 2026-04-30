package ru.yandex.practicum.mymarket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.Map;

@Component
public class CartItemEnricher {
    private static final Logger log = LoggerFactory.getLogger(CartItemEnricher.class);
    private static final String CART_ATTRIBUTE = "cart";

    private final ItemService itemService;

    public CartItemEnricher(ItemService itemService) {
        this.itemService = itemService;
    }

    public Flux<Item> getCartItems(WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        log.debug("Getting all cart items, cart size: {} in session {}", cart.size(), session.getId());
        return Flux.fromIterable(cart.entrySet())
                .doOnNext(entry -> log.debug("Processing cart entry: itemId={}, count={}", entry.getKey(), entry.getValue()))
                .flatMap(entry -> itemService.getItemById(entry.getKey())
                        .doOnNext(item -> log.debug("Found item: {}", item.getTitle()))
                        .map(item -> {
                            item.setCount(entry.getValue());
                            return item;
                        }));
    }

    public Mono<Long> getTotalSum(WebSession session) {
        log.debug("Calculating total sum of cart in session {}", session.getId());
        return getCartItems(session)
                .map(item -> {
                    long sum = item.getPrice() * item.getCount();
                    log.debug("Item {}: {} * {} = {}", item.getTitle(), item.getPrice(), item.getCount(), sum);
                    return sum;
                })
                .reduce(0L, Long::sum)
                .doOnNext(total -> log.info("Cart total sum: {} in session {}", total, session.getId()));
    }

    private Map<Long, Integer> getCartFromSession(WebSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = session.getAttribute(CART_ATTRIBUTE);
        if (cart == null) {
            cart = new java.util.HashMap<>();
            session.getAttributes().put(CART_ATTRIBUTE, cart);
            log.debug("Created new cart for session: {}", session.getId());
        }
        return cart;
    }
}