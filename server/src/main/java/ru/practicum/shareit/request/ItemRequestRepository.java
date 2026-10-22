package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestor_IdOrderByCreatedDescIdDesc(long requestorId);

    List<ItemRequest> findByRequestor_IdNotOrderByCreatedDescIdDesc(long requestorId);
}
