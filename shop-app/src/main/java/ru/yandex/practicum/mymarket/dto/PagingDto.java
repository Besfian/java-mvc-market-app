package ru.yandex.practicum.mymarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagingDto {
    private int pageSize;
    private int pageNumber;
    private boolean hasPrevious;
    private boolean hasNext;

    public int pageSize() { return pageSize; }
    public int pageNumber() { return pageNumber; }
    public boolean hasPrevious() { return hasPrevious; }
    public boolean hasNext() { return hasNext; }
}