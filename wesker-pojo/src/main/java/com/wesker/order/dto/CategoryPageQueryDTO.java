package com.wesker.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryPageQueryDTO(
        @Size(max = 32, message = "分类名称最多 32 个字符") String name,
        @Min(value = 1, message = "分类类型只能是菜品分类或套餐分类")
        @Max(value = 2, message = "分类类型只能是菜品分类或套餐分类") Integer type,
        @NotNull(message = "请填写页码") @Min(value = 1, message = "页码至少为 1") Integer page,
        @NotNull(message = "请填写每页条数") @Min(value = 1, message = "每页至少 1 条")
        @Max(value = 100, message = "每页最多 100 条") Integer pageSize
) {
}
