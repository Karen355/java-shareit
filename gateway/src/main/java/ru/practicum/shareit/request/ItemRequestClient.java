package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@Service
public class ItemRequestClient extends BaseClient {
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(buildRestTemplate(serverUrl + "/requests", builder));
    }

    public ResponseEntity<Object> create(long userId, CreateItemRequestDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("/all", userId);
    }

    public ResponseEntity<Object> get(long userId, long requestId) {
        return get("/" + requestId, userId);
    }
}
