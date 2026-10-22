package ru.practicum.shareit.request;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requests;
    private final ItemRepository items;
    private final UserRepository users;

    public ItemRequestServiceImpl(ItemRequestRepository requests, ItemRepository items, UserRepository users) {
        this.requests = requests;
        this.items = items;
        this.users = users;
    }

    @Override
    public ItemRequestDto create(long userId, CreateItemRequestDto dto) {
        User requestor = findUser(userId);
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request description is empty");
        }
        ItemRequest request = requests.save(ItemRequestMapper.toModel(dto, requestor));
        return ItemRequestMapper.toDto(request, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwn(long userId) {
        findUser(userId);
        return toDtos(requests.findByRequestor_IdOrderByCreatedDescIdDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(long userId) {
        findUser(userId);
        return toDtos(requests.findByRequestor_IdNotOrderByCreatedDescIdDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto get(long userId, long requestId) {
        findUser(userId);
        ItemRequest request = requests.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Request with id " + requestId + " not found"));
        return ItemRequestMapper.toDto(request, items.findByRequest_IdOrderByIdAsc(requestId));
    }

    private List<ItemRequestDto> toDtos(List<ItemRequest> found) {
        if (found.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = found.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequest = items.findByRequest_IdInOrderByIdAsc(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return found.stream()
                .map(request -> ItemRequestMapper.toDto(request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    private User findUser(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with id " + userId + " not found"));
    }
}
