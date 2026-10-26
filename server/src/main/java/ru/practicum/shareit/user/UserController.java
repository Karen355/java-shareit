package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto dto) {
        log.info("Creating user");
        return service.create(dto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId, @RequestBody UserDto dto) {
        log.info("Updating user id={}", userId);
        return service.update(userId, dto);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable long userId) {
        log.info("Getting user id={}", userId);
        return service.get(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Getting all users");
        return service.getAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        log.info("Deleting user id={}", userId);
        service.delete(userId);
    }
}
