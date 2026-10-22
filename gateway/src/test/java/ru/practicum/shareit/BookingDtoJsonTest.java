package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private ObjectMapper mapper;

    @Test
    void bookingDatesUseIsoLocalDateTimeInJson() throws Exception {
        LocalDateTime start = LocalDateTime.parse("2030-03-10T12:30:00");
        LocalDateTime end = start.plusHours(2);
        BookItemRequestDto dto = new BookItemRequestDto(7L, start, end);

        String json = mapper.writeValueAsString(dto);

        assertEquals("2030-03-10T12:30:00", mapper.readTree(json).get("start").asText());
        assertEquals("2030-03-10T14:30:00", mapper.readTree(json).get("end").asText());
        assertEquals(dto.getItemId(), mapper.readValue(json, BookItemRequestDto.class).getItemId());
    }
}
