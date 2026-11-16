package com.wesker.order.entity;

import java.time.LocalDateTime;

public record Employee(
        Long id,
        String name,
        String username,
        String phone,
        String sex,
        String idNumber,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        Long createUser,
        Long updateUser
) {
}
