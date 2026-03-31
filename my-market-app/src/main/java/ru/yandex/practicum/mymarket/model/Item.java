package ru.yandex.practicum.mymarket.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_title", columnList = "title")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "img_path", nullable = false, length = 500)
    private String imgPath;

    @Column(name = "price", nullable = false)
    private Long price;

    @Transient
    private int count;

    public Item(Long id) {
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String imgPath() {
        return imgPath;
    }

    public Long price() {
        return price;
    }

    public int count() {
        return count;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id != null && id.equals(item.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}