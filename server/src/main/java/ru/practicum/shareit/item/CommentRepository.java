package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.item.id IN :itemIds
            ORDER BY c.created ASC, c.id ASC
            """)
    List<Comment> findByItems(@Param("itemIds") Collection<Long> itemIds);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.item.id = :itemId
            ORDER BY c.created ASC, c.id ASC
            """)
    List<Comment> findByItem_IdOrderByCreatedAsc(@Param("itemId") long itemId);
}
