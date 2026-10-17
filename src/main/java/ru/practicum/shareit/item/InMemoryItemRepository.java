package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId.incrementAndGet());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .sorted(Comparator.comparing(Item::getId))
                .toList();
    }

    @Override
    public List<Item> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String query = text.toLowerCase(Locale.ROOT);
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase(Locale.ROOT).contains(query)
                        || item.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(Item::getId))
                .toList();
    }
}
