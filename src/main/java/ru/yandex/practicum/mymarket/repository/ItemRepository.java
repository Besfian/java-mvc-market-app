package ru.yandex.practicum.mymarket.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    @Query("SELECT * FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'ALPHA' THEN title END ASC, " +
            "CASE WHEN :sort = 'PRICE' THEN price END ASC, " +
            "CASE WHEN :sort = 'NO' THEN id END ASC " +
            "LIMIT :limit OFFSET :offset")
    Flux<Item> searchItems(String search, String sort, int limit, long offset);

    @Query("SELECT * FROM items " +
            "ORDER BY " +
            "CASE WHEN :sort = 'ALPHA' THEN title END ASC, " +
            "CASE WHEN :sort = 'PRICE' THEN price END ASC, " +
            "CASE WHEN :sort = 'NO' THEN id END ASC " +
            "LIMIT :limit OFFSET :offset")
    Flux<Item> findAllWithSort(String sort, int limit, long offset);

    @Query("SELECT COUNT(*) FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Mono<Long> countBySearch(String search);

    Mono<Long> count();
}