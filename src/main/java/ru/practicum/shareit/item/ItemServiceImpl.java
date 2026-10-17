package ru.practicum.shareit.item;

import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final CommentRepository comments;
    private final Validator validator;

    public ItemServiceImpl(ItemRepository repository, UserRepository users, BookingRepository bookings,
                           CommentRepository comments, Validator validator) {
        this.repository = repository;
        this.users = users;
        this.bookings = bookings;
        this.comments = comments;
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
    public ItemDetailsDto get(long itemId, Long viewerId) {
        Item item = find(itemId);
        if (viewerId != null && item.getOwner().getId().equals(viewerId)) {
            LocalDateTime now = LocalDateTime.now();
            return ItemMapper.toDetailsDto(item,
                    bookings.findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
                            itemId, BookingStatus.APPROVED, now).orElse(null),
                    bookings.findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                            itemId, BookingStatus.APPROVED, now).orElse(null),
                    getComments(itemId));
        }
        return ItemMapper.toDetailsDto(item, null, null, getComments(itemId));
    }

    @Override
    public List<OwnerItemDto> getOwnerItems(long ownerId) {
        findUser(ownerId);
        LocalDateTime now = LocalDateTime.now();
        return repository.findByOwner_IdOrderByIdAsc(ownerId).stream()
                .map(item -> ItemMapper.toOwnerDto(item,
                        bookings.findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
                                item.getId(), BookingStatus.APPROVED, now).orElse(null),
                        bookings.findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                                item.getId(), BookingStatus.APPROVED, now).orElse(null),
                        getComments(item.getId())))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return repository.searchAvailable(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto addComment(long authorId, long itemId, CreateCommentDto dto) {
        User author = findUser(authorId);
        Item item = find(itemId);
        if (!bookings.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, authorId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No completed booking for this item and user");
        }
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toDto(comments.save(comment));
    }

    private List<CommentDto> getComments(long itemId) {
        return comments.findByItem_IdOrderByCreatedAsc(itemId).stream()
                .map(CommentMapper::toDto)
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
