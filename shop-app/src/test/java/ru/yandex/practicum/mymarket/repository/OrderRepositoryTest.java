package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import reactor.core.publisher.Flux;

@DataR2dbcTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void saveShouldPersistOrder() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        Mono<Order> savedMono = orderRepository.save(order);
        StepVerifier.create(savedMono)
                .expectNextMatches(saved -> saved.getId() != null)
                .verifyComplete();
    }

    @Test
    void findAllShouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setCreatedAt(LocalDateTime.now());
        Order order2 = new Order();
        order2.setCreatedAt(LocalDateTime.now());
        Flux<Order> savedOrders = orderRepository.saveAll(Flux.just(order1, order2));
        StepVerifier.create(savedOrders.collectList())
                .expectNextMatches(orders -> orders.size() == 2)
                .verifyComplete();
    }

    @Test
    void findByIdWhenExistsShouldReturnOrder() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        Mono<Order> savedMono = orderRepository.save(order);
        StepVerifier.create(savedMono.flatMap(saved -> orderRepository.findById(saved.getId())))
                .expectNextMatches(found -> found.getId() != null)
                .verifyComplete();
    }

    @Test
    void deleteByIdShouldRemoveOrder() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        Mono<Order> savedMono = orderRepository.save(order);
        StepVerifier.create(savedMono
                        .flatMap(saved -> orderRepository.deleteById(saved.getId())
                                .then(orderRepository.findById(saved.getId()))))
                .verifyComplete();
    }
}