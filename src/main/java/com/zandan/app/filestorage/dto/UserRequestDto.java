package com.zandan.app.filestorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDto(

        @NotBlank(message = "{user.username.not_blank}")
        @Size(min = 5, max = 128, message = "{user.username_invalid_size}")
        String username,

        @NotBlank(message = "{user.password.not_blank}")
        @Size(min = 5, max = 128, message = "{user.password.invalid_size}")
        String password
) {
}
