package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@RestController
@RequestMapping("/requests")
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient client;

    public ItemRequestController(ItemRequestClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @Valid @RequestBody CreateItemRequestDto dto) {
        log.info("Creating item request for user id={}", userId);
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Getting item requests for user id={}", userId);
        return client.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Getting other item requests for user id={}", userId);
        return client.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                      @PathVariable @Positive long requestId) {
        log.info("Getting item request id={} for user id={}", requestId, userId);
        return client.get(userId, requestId);
    }
}
