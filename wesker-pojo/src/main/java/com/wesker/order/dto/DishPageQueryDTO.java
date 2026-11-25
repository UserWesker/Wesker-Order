package com.wesker.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// 菜品管理列表的分页参数和可选筛选条件。
public record DishPageQueryDTO(
        @NotNull(message = "请填写页码")
        @Min(value = 1, message = "页码至少为 1") Integer page,
        @NotNull(message = "请填写每页条数")
        @Min(value = 1, message = "每页至少 1 条")
        @Max(value = 100, message = "每页最多 100 条") Integer pageSize,
        @Size(max = 32, message = "查询关键词最多 32 个字符") String name,
        @Positive(message = "分类 ID 必须大于 0") Long categoryId,
        @Min(value = 0, message = "菜品状态只能为 0（停售）或 1（起售）")
        @Max(value = 1, message = "菜品状态只能为 0（停售）或 1（起售）") Integer status
) {
}
