package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookings;
    private final ItemRepository items;
    private final UserRepository users;

    public BookingServiceImpl(BookingRepository bookings, ItemRepository items, UserRepository users) {
        this.bookings = bookings;
        this.items = items;
        this.users = users;
    }

    @Override
    public BookingDto create(long bookerId, CreateBookingDto dto) {
        User booker = findUser(bookerId);
        Item item = items.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item with id " + dto.getItemId() + " not found"));
        if (item.getOwner().getId() == bookerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "An owner cannot book their own item");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not available");
        }
        LocalDateTime now = LocalDateTime.now();
        if (dto.getStart() == null || dto.getEnd() == null
                || !dto.getStart().isAfter(now) || !dto.getEnd().isAfter(dto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking dates");
        }
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toDto(bookings.save(booking));
    }

    @Override
    public BookingDto decide(long ownerId, long bookingId, boolean approved) {
        Booking booking = findBooking(bookingId);
        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the item owner may decide a booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already decided");
        }
        if (approved && bookings.existsByItem_IdAndStatusAndStartLessThanAndEndGreaterThan(
                booking.getItem().getId(), BookingStatus.APPROVED, booking.getEnd(), booking.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is already booked for these dates");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookings.save(booking));
    }

    @Override
    public BookingDto get(long userId, long bookingId) {
        findUser(userId);
        Booking booking = findBooking(bookingId);
        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking is not accessible to this user");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getForBooker(long bookerId, String state) {
        findUser(bookerId);
        BookingSearchState searchState = parseState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> found = switch (searchState) {
            case ALL -> bookings.findByBooker_IdOrderByStartDesc(bookerId);
            case CURRENT -> bookings.findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                    bookerId, now, now);
            case PAST -> bookings.findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now);
            case FUTURE -> bookings.findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now);
            case WAITING -> bookings.findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case REJECTED -> bookings.findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
        };
        return found.stream().map(BookingMapper::toDto).toList();
    }

    @Override
    public List<BookingDto> getForOwner(long ownerId, String state) {
        findUser(ownerId);
        if (!items.existsByOwner_Id(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User owns no items: id " + ownerId);
        }
        BookingSearchState searchState = parseState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> found = switch (searchState) {
            case ALL -> bookings.findByItem_Owner_IdOrderByStartDesc(ownerId);
            case CURRENT -> bookings.findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                    ownerId, now, now);
            case PAST -> bookings.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookings.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING -> bookings.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED -> bookings.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        };
        return found.stream().map(BookingMapper::toDto).toList();
    }

    private BookingSearchState parseState(String state) {
        try {
            return BookingSearchState.valueOf(state.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown booking state: " + state);
        }
    }

    private User findUser(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with id " + userId + " not found"));
    }

    private Booking findBooking(long bookingId) {
        return bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking with id " + bookingId + " not found"));
    }
}
