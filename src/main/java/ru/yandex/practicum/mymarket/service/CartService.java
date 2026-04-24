package ru.yandex.practicum.mymarket.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final Map<Long, Integer> cart = new ConcurrentHashMap<>();

    public Mono<Integer> getItemCount(Long itemId) {
        Integer count = cart.getOrDefault(itemId, 0);
        log.debug("Getting count for item {}: {}", itemId, count);
        return Mono.just(count);
    }

    public Mono<Integer> increaseItem(Long itemId) {
        int newCount = cart.merge(itemId, 1, Integer::sum);
        log.info("Increased item {} to count: {}", itemId, newCount);
        return Mono.just(newCount);
    }

    public Mono<Integer> decreaseItem(Long itemId) {
        Integer oldCount = cart.get(itemId);
        if (oldCount != null && oldCount > 1) {
            int newCount = oldCount - 1;
            cart.put(itemId, newCount);
            log.info("Decreased item {} from {} to {}", itemId, oldCount, newCount);
            return Mono.just(newCount);
        } else if (oldCount != null && oldCount == 1) {
            cart.remove(itemId);
            log.info("Removed item {} from cart (count was 1)", itemId);
            return Mono.just(0);
        } else {
            log.warn("Attempted to decrease non-existent item {}", itemId);
            return Mono.just(0);
        }
    }

    public Mono<Void> removeItem(Long itemId) {
        Integer removed = cart.remove(itemId);
        if (removed != null) {
            log.info("Removed item {} from cart (had {} items)", itemId, removed);
        } else {
            log.warn("Attempted to remove non-existent item {}", itemId);
        }
        return Mono.empty();
    }

    public Mono<Void> clear() {
        log.info("Clearing entire cart, had {} items", cart.size());
        cart.clear();
        return Mono.empty();
    }

    public Flux<Item> getCartItems(ItemService itemService) {
        log.debug("Getting all cart items, cart size: {}", cart.size());
        return Flux.fromIterable(cart.entrySet())
                .doOnNext(entry -> log.debug("Processing cart entry: itemId={}, count={}", entry.getKey(), entry.getValue()))
                .flatMap(entry -> itemService.getItemById(entry.getKey())
                        .doOnNext(item -> log.debug("Found item: {}", item.getTitle()))
                        .map(item -> {
                            item.setCount(entry.getValue());
                            return item;
                        }));
    }

    public Mono<Long> getTotalSum(ItemService itemService) {
        log.debug("Calculating total sum of cart");
        return getCartItems(itemService)
                .map(item -> {
                    long sum = item.getPrice() * item.getCount();
                    log.debug("Item {}: {} * {} = {}", item.getTitle(), item.getPrice(), item.getCount(), sum);
                    return sum;
                })
                .reduce(0L, Long::sum)
                .doOnNext(total -> log.info("Cart total sum: {}", total));
    }
}