package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto dto);

    UserDto update(long id, UserDto dto);

    UserDto get(long id);

    List<UserDto> getAll();

    void delete(long id);
}
