package com.wesker.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeLoginDTO(
        @NotBlank @Size(max = 32) String username,
        @NotBlank String password
) {
}
