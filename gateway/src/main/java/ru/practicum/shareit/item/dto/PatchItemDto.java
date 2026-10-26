package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatchItemDto {
    @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @Size(max = 255)
    private String name;

    @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @Size(max = 1024)
    private String description;

    private Boolean available;
}
