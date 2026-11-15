package com.wesker.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeePageQueryDTO(
        @Size(max = 32, message = "员工姓名最多 32 个字符") String name,
        @NotNull(message = "请填写页码") @Min(value = 1, message = "页码必须大于等于 1") Integer page,
        @NotNull(message = "请填写每页条数") @Min(value = 1, message = "每页至少 1 条")
        @Max(value = 100, message = "每页最多 100 条") Integer pageSize
) {
}
