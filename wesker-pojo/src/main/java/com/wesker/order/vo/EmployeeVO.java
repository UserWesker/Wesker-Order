package com.wesker.order.vo;

import java.time.LocalDateTime;

public record EmployeeVO(
        Long id,
        String username,
        String name,
        String phone,
        String sex,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
