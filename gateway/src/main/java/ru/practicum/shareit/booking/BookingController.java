package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@Slf4j
@Validated
public class BookingController {
    private final BookingClient client;

    public BookingController(BookingClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @Valid @RequestBody BookItemRequestDto dto) {
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking dates");
        }
        log.info("Creating booking for item id={} by user id={}", dto.getItemId(), userId);
        return client.bookItem(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> decide(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable @Positive long bookingId, @RequestParam boolean approved) {
        log.info("Deciding booking id={} by owner id={}, approved={}", bookingId, userId, approved);
        return client.decide(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                      @PathVariable @Positive long bookingId) {
        log.info("Getting booking id={} for user id={}", bookingId, userId);
        return client.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getForBooker(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        String parsed = parseState(state);
        log.info("Getting bookings for user id={}, state={}", userId, parsed);
        return client.getBookings(userId, parsed);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getForOwner(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                               @RequestParam(defaultValue = "ALL") String state) {
        String parsed = parseState(state);
        log.info("Getting bookings for owner id={}, state={}", userId, parsed);
        return client.getOwnerBookings(userId, parsed);
    }

    private String parseState(String state) {
        return BookingState.from(state)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown booking state: " + state))
                .name();
    }
}
