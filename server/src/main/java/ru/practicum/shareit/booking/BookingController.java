package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") long bookerId,
                             @Valid @RequestBody CreateBookingDto dto) {
        log.info("Creating booking for item id={} by user id={}", dto.getItemId(), bookerId);
        return service.create(bookerId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto decide(@RequestHeader("X-Sharer-User-Id") long ownerId,
                             @PathVariable long bookingId, @RequestParam boolean approved) {
        log.info("Deciding booking id={} by owner id={}, approved={}", bookingId, ownerId, approved);
        return service.decide(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        log.info("Getting booking id={} for user id={}", bookingId, userId);
        return service.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getForBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(defaultValue = "ALL") String state) {
        log.info("Getting bookings for user id={}, state={}", userId, state);
        return service.getForBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(defaultValue = "ALL") String state) {
        log.info("Getting bookings for owner id={}, state={}", userId, state);
        return service.getForOwner(userId, state);
    }
}
