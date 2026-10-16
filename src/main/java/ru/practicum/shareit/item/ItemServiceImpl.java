package ru.practicum.shareit.item;

import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.Locale;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserService users;
    private final Validator validator;

    public ItemServiceImpl(ItemRepository repository, UserService users, Validator validator) {
        this.repository = repository;
        this.users = users;
        this.validator = validator;
    }

    @Override
    public ItemDto create(long ownerId, ItemDto dto) {
        Item item = ItemMapper.toModel(dto, users.getModel(ownerId));
        item.setId(null);
        return ItemMapper.toDto(repository.save(item));
    }

    @Override
    public ItemDto update(long ownerId, long itemId, ItemDto dto) {
        users.getModel(ownerId);
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
        users.getModel(ownerId);
        return repository.findAll().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String query = text.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase(Locale.ROOT).contains(query)
                        || item.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .map(ItemMapper::toDto)
                .toList();
    }

    private Item find(long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    private void validateProperty(ItemDto dto, String property) {
        if (!validator.validateProperty(dto, property).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + property);
        }
    }
}
