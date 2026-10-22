package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateItemRequestDto {
    @NotBlank
    @Size(max = 1024)
    private String description;
}
