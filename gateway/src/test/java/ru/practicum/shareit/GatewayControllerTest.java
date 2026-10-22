package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = {UserController.class, ItemController.class,
        BookingController.class, ItemRequestController.class})
class GatewayControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserClient users;

    @MockBean
    private ItemClient items;

    @MockBean
    private BookingClient bookings;

    @MockBean
    private ItemRequestClient requests;

    private final ResponseEntity<Object> ok = ResponseEntity.ok((Object) "ok");

    @Test
    void forwardsClientErrorBody() throws Exception {
        when(users.get(42)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User with id 42 not found")));

        mvc.perform(get("/users/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id 42 not found"));
    }

    @Test
    void userRoutesReachClient() throws Exception {
        when(users.create(any(UserDto.class))).thenReturn(ok);
        when(users.update(anyLong(), any(PatchUserDto.class))).thenReturn(ok);
        when(users.get(1)).thenReturn(ok);
        when(users.getAll()).thenReturn(ok);
        when(users.remove(1)).thenReturn(ok);

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
        verify(users).update(anyLong(), any(PatchUserDto.class));
        verify(users).get(1);
        verify(users).getAll();
        verify(users).remove(1);
    }

    @Test
    void itemRoutesReachClient() throws Exception {
        when(items.create(anyLong(), any(ItemDto.class))).thenReturn(ok);
        when(items.update(anyLong(), anyLong(), any(PatchItemDto.class))).thenReturn(ok);
        when(items.get(2, 1L)).thenReturn(ok);
        when(items.getOwnerItems(1)).thenReturn(ok);
        when(items.search("drill")).thenReturn(ok);
        when(items.addComment(anyLong(), anyLong(), any(CreateCommentDto.class))).thenReturn(ok);

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

        verify(items).create(anyLong(), any(ItemDto.class));
        verify(items).update(anyLong(), anyLong(), any(PatchItemDto.class));
        verify(items).get(2, 1L);
        verify(items).getOwnerItems(1);
        verify(items).search("drill");
        verify(items).addComment(anyLong(), anyLong(), any(CreateCommentDto.class));
    }

    @Test
    void bookingRoutesReachClient() throws Exception {
        when(bookings.bookItem(anyLong(), any(BookItemRequestDto.class))).thenReturn(ok);
        when(bookings.decide(1, 2, true)).thenReturn(ok);
        when(bookings.getBooking(1, 2L)).thenReturn(ok);
        when(bookings.getBookings(1, "ALL")).thenReturn(ok);
        when(bookings.getOwnerBookings(1, "WAITING")).thenReturn(ok);
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
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1)
                        .param("state", "waiting"))
                .andExpect(status().isOk());

        verify(bookings).bookItem(anyLong(), any(BookItemRequestDto.class));
        verify(bookings).decide(1, 2, true);
        verify(bookings).getBooking(1, 2L);
        verify(bookings).getBookings(1, "ALL");
        verify(bookings).getOwnerBookings(1, "WAITING");
    }

    @Test
    void requestRoutesReachClient() throws Exception {
        when(requests.create(anyLong(), any(CreateItemRequestDto.class))).thenReturn(ok);
        when(requests.getOwn(1)).thenReturn(ok);
        when(requests.getAll(1)).thenReturn(ok);
        when(requests.get(1, 2)).thenReturn(ok);

        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/requests/2").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());

        verify(requests).create(anyLong(), any(CreateItemRequestDto.class));
        verify(requests).getOwn(1);
        verify(requests).getAll(1);
        verify(requests).get(1, 2);
    }

    @Test
    void invalidInputStopsAtGateway() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/items").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"description\":\"Tool\",\"available\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":2,\"start\":null,\"end\":null}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/items/2").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/items").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Tool\",\"available\":true,\"requestId\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + "x".repeat(1025) + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 1).param("state", "invalid"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/users/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/requests/-1").header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/items").header("X-Sharer-User-Id", -1))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));

        verifyNoInteractions(users, items, bookings, requests);
    }
}
