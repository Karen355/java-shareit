package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT b FROM Booking b JOIN FETCH b.booker
            WHERE b.item.id IN :itemIds AND b.status = :status
            ORDER BY b.start DESC, b.id DESC
            """)
    List<Booking> findByItemsAndStatus(@Param("itemIds") Collection<Long> itemIds,
                                       @Param("status") BookingStatus status);

    List<Booking> findByBooker_IdOrderByStartDesc(long bookerId);

    List<Booking> findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    List<Booking> findByItem_Owner_IdOrderByStartDesc(long ownerId);

    List<Booking> findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(long ownerId, BookingStatus status);

    Optional<Booking> findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
            long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
            long itemId, long bookerId, BookingStatus status, LocalDateTime now);

    boolean existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
            long itemId, BookingStatus status, LocalDateTime end, LocalDateTime start);
}
