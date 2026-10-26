package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;

@RestController
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient client;

    public ItemController(ItemClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @Valid @RequestBody ItemDto dto) {
        log.info("Creating item for user id={}", userId);
        return client.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable @Positive long itemId, @Valid @RequestBody PatchItemDto dto) {
        log.info("Updating item id={} by user id={}", itemId, userId);
        return client.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable @Positive long itemId,
                                      @RequestHeader(value = "X-Sharer-User-Id", required = false) @Positive Long userId) {
        log.info("Getting item id={}", itemId);
        return client.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Getting items for user id={}", userId);
        return client.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Searching items by text={}", text);
        return client.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                             @PathVariable @Positive long itemId,
                                             @Valid @RequestBody CreateCommentDto dto) {
        log.info("Adding comment to item id={} by user id={}", itemId, userId);
        return client.addComment(userId, itemId, dto);
    }
}
