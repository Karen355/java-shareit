package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwner_IdOrderByIdAsc(long ownerId);

    boolean existsByOwner_Id(long ownerId);

    @Query("""
            SELECT i FROM Item i
            WHERE i.available = true
              AND (LOCATE(LOWER(:text), LOWER(i.name)) > 0
                   OR LOCATE(LOWER(:text), LOWER(i.description)) > 0)
            ORDER BY i.id
            """)
    List<Item> searchAvailable(@Param("text") String text);
}
