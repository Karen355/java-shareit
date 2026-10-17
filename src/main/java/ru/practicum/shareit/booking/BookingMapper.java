package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

public final class BookingMapper {
    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                ItemMapper.toDto(booking.getItem()), UserMapper.toDto(booking.getBooker()), booking.getStatus());
    }
}
