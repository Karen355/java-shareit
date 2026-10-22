package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.PatchUserDto;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserClient client;

    public UserController(UserClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto dto) {
        log.info("Creating user");
        return client.create(dto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable @Positive long userId, @Valid @RequestBody PatchUserDto dto) {
        log.info("Updating user id={}", userId);
        return client.update(userId, dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> get(@PathVariable @Positive long userId) {
        log.info("Getting user id={}", userId);
        return client.get(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Getting all users");
        return client.getAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable @Positive long userId) {
        log.info("Deleting user id={}", userId);
        return client.remove(userId);
    }
}
