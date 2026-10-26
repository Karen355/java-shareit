package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b.item.id FROM Booking b WHERE b.id = :bookingId")
    Optional<Long> findItemIdByBookingId(@Param("bookingId") long bookingId);

    @Query("""
            SELECT b FROM Booking b JOIN FETCH b.booker
            WHERE b.item.id IN :itemIds AND b.status = :status
            ORDER BY b.start DESC, b.id DESC
            """)
    List<Booking> findByItemsAndStatus(@Param("itemIds") Collection<Long> itemIds,
                                       @Param("status") BookingStatus status);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdOrderByStartDesc(long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            long bookerId, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdOrderByStartDesc(long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            long ownerId, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
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
