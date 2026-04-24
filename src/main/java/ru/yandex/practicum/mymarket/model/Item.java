package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("items")
public class Item {
    @Id
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private Long price;

    @Transient
    private int count;

    public Long getId() { return id; }
    public Long id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String imgPath() { return imgPath; }
    public Long price() { return price; }
    public int count() { return count; }
}