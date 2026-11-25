package com.wesker.order.dto;

import com.wesker.order.entity.DishFlavor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record DishDTO(
        @Positive(message = "菜品 ID 必须大于 0") Long id,
        @NotBlank(message = "请填写菜品名称")
        @Size(max = 32, message = "菜品名称最多 32 个字符") String name,
        @NotNull(message = "请选择菜品分类")
        @Positive(message = "分类 ID 必须大于 0") Long categoryId,
        @NotNull(message = "请填写菜品价格")
        @DecimalMin(value = "0.01", message = "菜品价格至少为 0.01")
        @Digits(integer = 8, fraction = 2, message = "菜品价格最多 8 位整数和 2 位小数") BigDecimal price,
        @NotBlank(message = "请上传菜品图片")
        @Size(max = 255, message = "图片对象键最多 255 个字符") String imageKey,
        @Size(max = 255, message = "菜品描述最多 255 个字符") String description,
        @Min(value = 0, message = "菜品状态只能为 0（停售）或 1（起售）")
        @Max(value = 1, message = "菜品状态只能为 0（停售）或 1（起售）") Integer status,
        List<@NotNull(message = "口味不能为 null") @Valid DishFlavor> flavors
) {
    public DishDTO {
        if (flavors == null) {
            flavors = List.of();
        }
    }
}
