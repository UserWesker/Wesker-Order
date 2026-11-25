package com.wesker.order.entity;

import java.time.LocalDateTime;

public record Category(
        Long id,
        String name,
        Integer type,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        Long createUser,
        Long updateUser
) {
}
