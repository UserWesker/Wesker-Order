package com.wesker.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDTO(
        @NotBlank(message = "请填写分类名称")
        @Size(max = 32, message = "分类名称最多 32 个字符") String name,
        @Min(value = 1, message = "分类类型只能是菜品分类或套餐分类")
        @Max(value = 2, message = "分类类型只能是菜品分类或套餐分类") Integer type
) {
    public CategoryDTO {
        if (name != null) {
            name = name.strip();
        }
    }
}
