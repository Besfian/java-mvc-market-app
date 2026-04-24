package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderShouldSaveOrder() {
        Item item = createTestItem();
        item.setCount(2);
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(any(Iterable.class))).thenReturn(Flux.empty());
        when(orderItemRepository.findByOrderId(any(Long.class))).thenReturn(Flux.empty());
        StepVerifier.create(orderService.createOrder(List.of(item)))
                .expectNextMatches(order -> order.getId() == 1L)
                .verifyComplete();
    }

    @Test
    void getAllOrdersShouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);
        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(any(Long.class))).thenReturn(Flux.empty());
        StepVerifier.create(orderService.getAllOrders().collectList())
                .expectNextMatches(orders -> orders.size() == 2)
                .verifyComplete();
    }

    @Test
    void getOrderByIdWhenExistsShouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.empty());
        StepVerifier.create(orderService.getOrderById(1L))
                .expectNextMatches(o -> o.getId() == 1L)
                .verifyComplete();
    }

    @Test
    void getOrderByIdWhenNotExistsShouldReturnEmpty() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(orderService.getOrderById(99L))
                .verifyComplete();
    }

    private Item createTestItem() {
        return Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
    }
}