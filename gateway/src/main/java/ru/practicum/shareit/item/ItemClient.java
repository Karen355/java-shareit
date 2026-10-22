package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(buildRestTemplate(serverUrl + "/items", builder));
    }

    public ResponseEntity<Object> create(long userId, ItemDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, PatchItemDto dto) {
        return patch("/" + itemId, userId, dto);
    }

    public ResponseEntity<Object> get(long itemId, Long userId) {
        return get("/" + itemId, userId, null);
    }

    public ResponseEntity<Object> getOwnerItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CreateCommentDto dto) {
        return post("/" + itemId + "/comment", userId, dto);
    }
}
