package ru.practicum.shareit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BookingRepository bookings;

    @Autowired
    private BookingService bookingService;

    @Test
    void overlappingBookingsCannotBothBeApprovedConcurrently() throws Exception {
        long ownerId = createUser("Concurrent owner", "booking.concurrent.owner@example.com");
        long firstBookerId = createUser("First booker", "booking.concurrent.first@example.com");
        long secondBookerId = createUser("Second booker", "booking.concurrent.second@example.com");
        long itemId = createItem(ownerId);
        LocalDateTime start = LocalDateTime.now().plusDays(2).withNano(0);
        long firstBookingId = createBooking(firstBookerId, itemId, start, start.plusDays(2));
        long secondBookingId = createBooking(secondBookerId, itemId, start.plusDays(1), start.plusDays(3));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Future<Boolean> first = executor.submit(() -> approveWhenReady(ownerId, firstBookingId, ready, go));
            Future<Boolean> second = executor.submit(() -> approveWhenReady(ownerId, secondBookingId, ready, go));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();

            int approvedCalls = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, approvedCalls);
            List<BookingStatus> statuses = List.of(bookings.findById(firstBookingId).orElseThrow().getStatus(),
                    bookings.findById(secondBookingId).orElseThrow().getStatus());
            assertEquals(1, statuses.stream().filter(status -> status == BookingStatus.APPROVED).count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void bookingApprovalListsAndCompletedReviewWorkTogether() throws Exception {
        long ownerId = createUser("Owner", "booking.owner@example.com");
        long bookerId = createUser("Booker", "booking.booker@example.com");
        long strangerId = createUser("Stranger", "booking.stranger@example.com");
        long itemId = createItem(ownerId);
        LocalDateTime start = LocalDateTime.now().plusDays(2).withNano(0);
        LocalDateTime end = start.plusDays(1);
        String request = mapper.writeValueAsString(new BookingInput(itemId, start, end));

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());
        String response = mvc.perform(post("/bookings").header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andReturn().getResponse().getContentAsString();
        long bookingId = mapper.readTree(response).get("id").asLong();

        mvc.perform(patch("/bookings/{id}", bookingId).param("approved", "true")
                        .header("X-Sharer-User-Id", strangerId))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/bookings/{id}", bookingId).param("approved", "true")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/bookings/{id}", bookingId).header("X-Sharer-User-Id", strangerId))
                .andExpect(status().isForbidden());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", bookerId).param("state", "FUTURE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", bookerId).param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/items").header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nextBooking.id").value(bookingId));
        mvc.perform(get("/items/{id}", itemId).header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nextBooking.id").value(bookingId));

        mvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Worked well\"}"))
                .andExpect(status().isBadRequest());

        Booking booking = bookings.findById(bookingId).orElseThrow();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookings.save(booking);

        mvc.perform(post("/items/{id}/comment", itemId).header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Worked well\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Booker"));
        mvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)));
        mvc.perform(get("/items").header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastBooking.id").value(bookingId))
                .andExpect(jsonPath("$[0].comments", hasSize(1)));
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", bookerId).param("state", "PAST"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void bookingDatesAreValidatedAndErrorsHaveACommonShape() throws Exception {
        long ownerId = createUser("Date owner", "booking.date.owner@example.com");
        long bookerId = createUser("Date booker", "booking.date.booker@example.com");
        long itemId = createItem(ownerId);
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingInput(itemId, past, future))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingInput(itemId, future, past))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingInput(itemId, future, future))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid booking dates"));
    }

    private long createUser(String name, String email) throws Exception {
        String response = mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserInput(name, email))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private long createItem(long ownerId) throws Exception {
        String response = mvc.perform(post("/items").header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Tool\",\"available\":true}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode item = mapper.readTree(response);
        return item.get("id").asLong();
    }

    private long createBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end) throws Exception {
        String response = mvc.perform(post("/bookings").header("X-Sharer-User-Id", bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingInput(itemId, start, end))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private boolean approveWhenReady(long ownerId, long bookingId, CountDownLatch ready, CountDownLatch go)
            throws InterruptedException {
        ready.countDown();
        go.await();
        try {
            bookingService.decide(ownerId, bookingId, true);
            return true;
        } catch (ResponseStatusException exception) {
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            return false;
        }
    }

    private record UserInput(String name, String email) {
    }

    private record BookingInput(long itemId, LocalDateTime start, LocalDateTime end) {
    }
}
