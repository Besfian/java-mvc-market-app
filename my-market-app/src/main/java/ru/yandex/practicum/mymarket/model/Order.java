package ru.yandex.practicum.mymarket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;




@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    public Order(List<OrderItem> items) {
        this.items = items;
        this.createdAt = LocalDateTime.now();
    }

    public Long id() {
        return id;
    }

    public List<OrderItem> items() {
        return items;
    }

    public Long totalSum() {
        return items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();
    }
}