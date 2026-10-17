package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingBriefDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingBriefDto lastBooking;
    private BookingBriefDto nextBooking;
    private List<CommentDto> comments;
}
