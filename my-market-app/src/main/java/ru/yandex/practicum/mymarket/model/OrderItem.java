package ru.yandex.practicum.mymarket.model;


import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "count", nullable = false)
    private int count;

    public OrderItem(Item item, int count) {
        this.itemId = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.count = count;
    }

    public String title() {
        return title;
    }

    public Long price() {
        return price;
    }

    public int count() {
        return count;
    }
}