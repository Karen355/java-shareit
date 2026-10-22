package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingBriefDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toDto(Item item) {
        Long requestId = item.getRequest() == null ? null : item.getRequest().getId();
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), requestId);
    }

    public static Item toModel(ItemDto dto, User owner, ItemRequest request) {
        return new Item(dto.getId(), dto.getName(), dto.getDescription(), dto.getAvailable(), owner, request);
    }

    public static ItemDetailsDto toDetailsDto(Item item, Booking last, Booking next, List<CommentDto> comments) {
        return new ItemDetailsDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(),
                toBriefDto(last), toBriefDto(next), comments);
    }

    public static OwnerItemDto toOwnerDto(Item item, Booking last, Booking next, List<CommentDto> comments) {
        return new OwnerItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(),
                toBriefDto(last), toBriefDto(next), comments);
    }

    private static BookingBriefDto toBriefDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingBriefDto(booking.getId(), booking.getBooker().getId(), booking.getStart(), booking.getEnd());
    }
}
