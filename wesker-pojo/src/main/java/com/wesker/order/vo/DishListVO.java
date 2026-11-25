package com.wesker.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DishListVO(
        Long id,
        String name,
        String categoryName,
        BigDecimal price,
        Integer status,
        LocalDateTime updateTime,
        String imageUrl,
        String thumbnailUrl
) {
}
