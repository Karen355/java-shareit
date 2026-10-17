package ru.practicum.shareit.item;

import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository users;
    private final Validator validator;

    public ItemServiceImpl(ItemRepository repository, UserRepository users, Validator validator) {
        this.repository = repository;
        this.users = users;
        this.validator = validator;
    }

    @Override
    public ItemDto create(long ownerId, ItemDto dto) {
        Item item = ItemMapper.toModel(dto, findUser(ownerId));
        item.setId(null);
        return ItemMapper.toDto(repository.save(item));
    }

    @Override
    public ItemDto update(long ownerId, long itemId, ItemDto dto) {
        findUser(ownerId);
        Item item = find(itemId);
        if (item.getOwner().getId() != ownerId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner may update an item");
        }
        if (dto.getName() != null) {
            validateProperty(dto, "name");
        }
        if (dto.getDescription() != null) {
            validateProperty(dto, "description");
        }
        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return ItemMapper.toDto(repository.save(item));
    }

    @Override
    public ItemDto get(long itemId) {
        return ItemMapper.toDto(find(itemId));
    }

    @Override
    public List<ItemDto> getOwnerItems(long ownerId) {
        findUser(ownerId);
        return repository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return repository.searchAvailable(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    private Item find(long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item with id " + itemId + " not found"));
    }

    private User findUser(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with id " + userId + " not found"));
    }

    private void validateProperty(ItemDto dto, String property) {
        if (!validator.validateProperty(dto, property).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + property);
        }
    }
}
