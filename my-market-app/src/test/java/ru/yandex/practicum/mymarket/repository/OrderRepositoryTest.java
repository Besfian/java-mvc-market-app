package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveShouldPersistOrder() {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(1L);  // Устанавливаем itemId
        orderItem.setTitle("Тестовый товар");
        orderItem.setPrice(1000L);
        orderItem.setCount(2);

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of(orderItem));

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void findAllShouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setCreatedAt(LocalDateTime.now());
        order1.setItems(List.of());

        Order order2 = new Order();
        order2.setCreatedAt(LocalDateTime.now());
        order2.setItems(List.of());

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> orders = orderRepository.findAll();

        assertThat(orders).hasSize(2);
    }

    @Test
    void findByIdWhenExistsShouldReturnOrder() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of());
        Order saved = orderRepository.save(order);

        Order found = orderRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void deleteByIdShouldRemoveOrder() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of());
        Order saved = orderRepository.save(order);

        orderRepository.deleteById(saved.getId());

        assertThat(orderRepository.findById(saved.getId())).isEmpty();
    }
}