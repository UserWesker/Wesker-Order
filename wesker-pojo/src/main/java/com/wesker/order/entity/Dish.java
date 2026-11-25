package com.wesker.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体，对应数据库中的 {@code dish} 表。
 *
 * @param id          菜品 ID，由数据库自增生成
 * @param categoryId  菜品分类 ID，对应 {@code category.id}
 * @param name        菜品名称
 * @param price       菜品售价，保留两位小数
 * @param imageKey    菜品图片在 OSS 中的对象键，例如 {@code dishes/latte.jpg}
 * @param description 菜品描述，可为空
 * @param status      销售状态，0 为停售，1 为起售
 * @param createTime  创建时间
 * @param updateTime  最后修改时间
 * @param createUser  创建菜品的管理员 ID
 * @param updateUser  最后修改菜品的管理员 ID
 */
public record Dish(
        Long id,
        Long categoryId,
        String name,
        BigDecimal price,
        String imageKey,
        String description,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        Long createUser,
        Long updateUser
) {
}
