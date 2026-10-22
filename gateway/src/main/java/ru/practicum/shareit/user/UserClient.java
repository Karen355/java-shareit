package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;

@Service
public class UserClient extends BaseClient {
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(buildRestTemplate(serverUrl + "/users", builder));
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post("", dto);
    }

    public ResponseEntity<Object> update(long userId, PatchUserDto dto) {
        return patch("/" + userId, dto);
    }

    public ResponseEntity<Object> get(long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }

    public ResponseEntity<Object> remove(long userId) {
        return delete("/" + userId);
    }
}
