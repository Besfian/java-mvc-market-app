package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Table("orders")
public class Order {
    @Id
    private Long id;
    private LocalDateTime createdAt;

    @Transient
    private List<OrderItem> items = new ArrayList<>();

    public Order(List<OrderItem> items) {
        this.items = items;
        this.createdAt = LocalDateTime.now();
    }

    public Long getTotalSum() {
        return items.stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum();
    }
}