package ru.yandex.practicum.mymarket.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Flux<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll()
                .doOnNext(order -> log.debug("Found order: {}", order.getId()))
                .flatMap(this::enrichOrderWithItems);
    }

    public Mono<Order> getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        return orderRepository.findById(id)
                .doOnNext(order -> log.debug("Found order: {}", id))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Order with id {} not found", id);
                    return Mono.empty();
                }))
                .flatMap(this::enrichOrderWithItems);
    }

    @Transactional
    public Mono<Order> createOrder(List<Item> cartItems) {
        log.info("Creating order from {} cart items", cartItems.size());
        List<OrderItem> orderItems = cartItems.stream()
                .filter(item -> {
                    boolean hasCount = item.getCount() > 0;
                    if (!hasCount) {
                        log.debug("Filtering out item {} with count 0", item.getTitle());
                    }
                    return hasCount;
                })
                .map(item -> {
                    log.debug("Creating OrderItem for {}: count={}, price={}", item.getTitle(), item.getCount(), item.getPrice());
                    return new OrderItem(item, item.getCount());
                })
                .collect(Collectors.toList());

        Order order = new Order(orderItems);
        order.setCreatedAt(LocalDateTime.now());
        log.debug("Order created with {} items", orderItems.size());

        return orderRepository.save(order)
                .doOnNext(savedOrder -> log.info("Order saved with id: {}", savedOrder.getId()))
                .flatMap(savedOrder -> {
                    orderItems.forEach(item -> item.setOrderId(savedOrder.getId()));
                    return orderItemRepository.saveAll(orderItems)
                            .doOnComplete(() -> log.debug("Saved {} order items for order {}", orderItems.size(), savedOrder.getId()))
                            .then(Mono.just(savedOrder));
                })
                .flatMap(savedOrder -> enrichOrderWithItems(savedOrder));
    }

    private Mono<Order> enrichOrderWithItems(Order order) {
        log.debug("Enriching order {} with items", order.getId());
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .doOnNext(items -> log.debug("Found {} items for order {}", items.size(), order.getId()))
                .map(items -> {
                    order.setItems(items);
                    return order;
                });
    }
}