package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @Valid @RequestBody CreateItemRequestDto dto) {
        log.info("Creating item request for user id={}", userId);
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Getting item requests for user id={}", userId);
        return service.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Getting other item requests for user id={}", userId);
        return service.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long requestId) {
        log.info("Getting item request id={} for user id={}", requestId, userId);
        return service.get(userId, requestId);
    }
}
