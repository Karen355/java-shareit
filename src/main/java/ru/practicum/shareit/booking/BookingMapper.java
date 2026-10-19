package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public final class BookingMapper {
    private BookingMapper() {
    }

    public static Booking toModel(CreateBookingDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                ItemMapper.toDto(booking.getItem()), UserMapper.toDto(booking.getBooker()), booking.getStatus());
    }
}
