package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(long bookerId, CreateBookingDto dto);

    BookingDto decide(long ownerId, long bookingId, boolean approved);

    BookingDto get(long userId, long bookingId);

    List<BookingDto> getForBooker(long bookerId, String state);

    List<BookingDto> getForOwner(long ownerId, String state);
}
