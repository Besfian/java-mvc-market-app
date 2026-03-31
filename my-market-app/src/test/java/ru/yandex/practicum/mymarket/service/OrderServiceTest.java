package ru.yandex.practicum.mymarket.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderShouldSaveOrder() {
        Item item = createTestItem();
        item.setCount(2);

        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(List.of(item));

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAllOrdersShouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);

        Order order2 = new Order();
        order2.setId(2L);

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<Order> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo(1L);
        assertThat(orders.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getOrderByIdWhenExistsShouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderByIdWhenNotExistsShouldThrowException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with id: 99");
    }

    private Item createTestItem() {
        return Item.builder()
                .id(1L)
                .title("Тестовый товар")
                .price(1000L)
                .build();
    }
}
