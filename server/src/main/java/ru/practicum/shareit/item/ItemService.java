package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long ownerId, ItemDto dto);

    ItemDto update(long ownerId, long itemId, ItemDto dto);

    ItemDetailsDto get(long itemId, Long viewerId);

    List<OwnerItemDto> getOwnerItems(long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(long authorId, long itemId, CreateCommentDto dto);
}
