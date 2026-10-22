package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequest toModel(CreateItemRequestDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        List<ItemResponseDto> responses = items.stream()
                .map(item -> new ItemResponseDto(item.getId(), item.getName(), item.getOwner().getId()))
                .toList();
        return new ItemRequestDto(request.getId(), request.getDescription(), request.getCreated(), responses);
    }
}
