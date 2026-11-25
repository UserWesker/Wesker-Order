package com.wesker.order.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 菜品口味实体，对应数据库中的 {@code dish_flavor} 表。
 *
 * @param id     口味记录 ID，由数据库自增生成
 * @param dishId 所属菜品 ID，对应 {@code dish.id}
 * @param name   口味名称，例如甜度
 * @param value  口味选项，例如 {@code ["无糖", "半糖"]}
 */
public record DishFlavor(
        Long id,
        Long dishId,
        @NotBlank(message = "请填写口味名称")
        @Size(max = 32, message = "口味名称最多 32 个字符") String name,
        @NotBlank(message = "请填写口味值")
        @Size(max = 255, message = "口味值最多 255 个字符") String value
) {
}

