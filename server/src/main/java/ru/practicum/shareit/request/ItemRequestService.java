package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, CreateItemRequestDto dto);

    List<ItemRequestDto> getOwn(long userId);

    List<ItemRequestDto> getAll(long userId);

    ItemRequestDto get(long userId, long requestId);
}
