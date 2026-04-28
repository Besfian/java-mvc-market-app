package ru.yandex.practicum.mymarket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private static final String CART_ATTRIBUTE = "cart";

    private Map<Long, Integer> getCartFromSession(WebSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = session.getAttribute(CART_ATTRIBUTE);
        if (cart == null) {
            cart = new HashMap<>();
            session.getAttributes().put(CART_ATTRIBUTE, cart);
            log.debug("Created new cart for session: {}", session.getId());
        }
        return cart;
    }

    public Mono<Integer> getItemCount(Long itemId, WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        Integer count = cart.getOrDefault(itemId, 0);
        log.debug("Getting count for item {}: {} in session {}", itemId, count, session.getId());
        return Mono.just(count);
    }

    public Mono<Integer> increaseItem(Long itemId, WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        int newCount = cart.merge(itemId, 1, Integer::sum);
        log.info("Increased item {} to count: {} in session {}", itemId, newCount, session.getId());
        return Mono.just(newCount);
    }

    public Mono<Integer> decreaseItem(Long itemId, WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        Integer oldCount = cart.get(itemId);
        if (oldCount != null && oldCount > 1) {
            int newCount = oldCount - 1;
            cart.put(itemId, newCount);
            log.info("Decreased item {} from {} to {} in session {}", itemId, oldCount, newCount, session.getId());
            return Mono.just(newCount);
        } else if (oldCount != null && oldCount == 1) {
            cart.remove(itemId);
            log.info("Removed item {} from cart (count was 1) in session {}", itemId, session.getId());
            return Mono.just(0);
        } else {
            log.warn("Attempted to decrease non-existent item {} in session {}", itemId, session.getId());
            return Mono.just(0);
        }
    }

    public Mono<Void> removeItem(Long itemId, WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        Integer removed = cart.remove(itemId);
        if (removed != null) {
            log.info("Removed item {} from cart (had {} items) in session {}", itemId, removed, session.getId());
        } else {
            log.warn("Attempted to remove non-existent item {} in session {}", itemId, session.getId());
        }
        return Mono.empty();
    }

    public Mono<Void> clear(WebSession session) {
        Map<Long, Integer> cart = getCartFromSession(session);
        log.info("Clearing entire cart, had {} items in session {}", cart.size(), session.getId());
        cart.clear();
        return Mono.empty();
    }

    public Flux<Item> getCartItems(ItemService itemService, WebSession session) {
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

    public Mono<Long> getTotalSum(ItemService itemService, WebSession session) {
        log.debug("Calculating total sum of cart in session {}", session.getId());
        return getCartItems(itemService, session)
                .map(item -> {
                    long sum = item.getPrice() * item.getCount();
                    log.debug("Item {}: {} * {} = {}", item.getTitle(), item.getPrice(), item.getCount(), sum);
                    return sum;
                })
                .reduce(0L, Long::sum)
                .doOnNext(total -> log.info("Cart total sum: {} in session {}", total, session.getId()));
    }
}