package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatchUserDto {
    @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @Size(max = 255)
    private String name;

    @Email
    @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @Size(max = 512)
    private String email;
}
