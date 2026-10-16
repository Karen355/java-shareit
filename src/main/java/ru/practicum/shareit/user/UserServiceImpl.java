package ru.practicum.shareit.user;

import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final Validator validator;

    public UserServiceImpl(UserRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public synchronized UserDto create(UserDto dto) {
        ensureEmailUnique(dto.getEmail(), null);
        User user = UserMapper.toModel(dto);
        user.setId(null);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public synchronized UserDto update(long id, UserDto dto) {
        User user = getModel(id);
        if (dto.getName() != null) {
            validateProperty(dto, "name");
        }
        if (dto.getEmail() != null) {
            validateProperty(dto, "email");
            ensureEmailUnique(dto.getEmail(), id);
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto get(long id) {
        return UserMapper.toDto(getModel(id));
    }

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream().map(UserMapper::toDto).toList();
    }

    @Override
    public void delete(long id) {
        getModel(id);
        repository.deleteById(id);
    }

    @Override
    public User getModel(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void ensureEmailUnique(String email, Long currentId) {
        boolean exists = repository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email) && !Objects.equals(user.getId(), currentId));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
    }

    private void validateProperty(UserDto dto, String property) {
        if (!validator.validateProperty(dto, property).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + property);
        }
    }
}
