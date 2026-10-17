package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long ownerId, ItemDto dto);

    ItemDto update(long ownerId, long itemId, ItemDto dto);

    ItemDto get(long itemId);

    List<ItemDto> getOwnerItems(long ownerId);

    List<ItemDto> search(String text);
}
