package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = {UserController.class, ItemController.class,
        BookingController.class, ItemRequestController.class})
class ServerControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService users;

    @MockBean
    private ItemService items;

    @MockBean
    private BookingService bookings;

    @MockBean
    private ItemRequestService requests;

    @Test
    void malformedRequestsHaveCommonErrorBody() throws Exception {
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(get("/bookings/nope").header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));

        verifyNoInteractions(users, items, bookings, requests);
    }

    @Test
    void userRoutesCallService() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/users/1")).andExpect(status().isOk());
        mvc.perform(get("/users")).andExpect(status().isOk());
        mvc.perform(delete("/users/1")).andExpect(status().isOk());

        verify(users).create(any(UserDto.class));
        verify(users).update(eq(1L), any(UserDto.class));
        verify(users).get(1);
        verify(users).getAll();
        verify(users).delete(1);
    }

    @Test
    void itemRoutesCallService() throws Exception {
        mvc.perform(post("/items").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Tool\",\"available\":true}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/items/2").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New drill\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/items/2").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/items").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/items/search").param("text", "drill")).andExpect(status().isOk());
        mvc.perform(post("/items/2/comment").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Good\"}"))
                .andExpect(status().isOk());

        verify(items).create(eq(1L), any(ItemDto.class));
        verify(items).update(eq(1L), eq(2L), any(ItemDto.class));
        verify(items).get(2, 1L);
        verify(items).getOwnerItems(1);
        verify(items).search("drill");
        verify(items).addComment(eq(1L), eq(2L), any(CreateCommentDto.class));
    }

    @Test
    void bookingRoutesCallService() throws Exception {
        String start = LocalDateTime.now().plusDays(2).withNano(0).toString();
        String end = LocalDateTime.now().plusDays(3).withNano(0).toString();
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":2,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/bookings/2").header("X-Sharer-User-Id", 1).param("approved", "true"))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings/2").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1).param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookings).create(eq(1L), any(CreateBookingDto.class));
        verify(bookings).decide(1, 2, true);
        verify(bookings).get(1, 2);
        verify(bookings).getForBooker(1, "ALL");
        verify(bookings).getForOwner(1, "WAITING");
    }

    @Test
    void requestRoutesCallService() throws Exception {
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/requests/2").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());

        verify(requests).create(eq(1L), any(CreateItemRequestDto.class));
        verify(requests).getOwn(1);
        verify(requests).getAll(1);
        verify(requests).get(1, 2);
    }
}
