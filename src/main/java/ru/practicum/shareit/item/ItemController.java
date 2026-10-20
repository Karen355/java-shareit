package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long ownerId, @Valid @RequestBody ItemDto dto) {
        log.info("Creating item for user id={}", ownerId);
        return service.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long ownerId,
                          @PathVariable long itemId, @RequestBody ItemDto dto) {
        log.info("Updating item id={} by user id={}", itemId, ownerId);
        return service.update(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ItemDetailsDto get(@PathVariable long itemId,
                              @RequestHeader(value = "X-Sharer-User-Id", required = false) Long viewerId) {
        log.info("Getting item id={}", itemId);
        return service.get(itemId, viewerId);
    }

    @GetMapping
    public List<OwnerItemDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        log.info("Getting items for user id={}", ownerId);
        return service.getOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Searching items by text={}", text);
        return service.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                 @PathVariable long itemId, @Valid @RequestBody CreateCommentDto dto) {
        log.info("Adding comment to item id={} by user id={}", itemId, authorId);
        return service.addComment(authorId, itemId, dto);
    }
}
