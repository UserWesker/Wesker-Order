package com.wesker.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeDTO(
        @NotBlank(message = "员工账号不能为空")
        @Size(max = 32, message = "员工账号不能超过 32 个字符") String username,
        @NotBlank(message = "员工姓名不能为空")
        @Size(max = 32, message = "员工姓名不能超过 32 个字符") String name,
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "1[3-9][0-9]{9}", message = "手机号必须为有效的 11 位手机号") String phone,
        @NotBlank(message = "请选择性别")
        @Pattern(regexp = "[01]", message = "性别只能为 1（男）或 0（女）") String sex,
        @NotBlank(message = "身份证号不能为空")
        @Pattern(regexp = "[1-9][0-9]{16}[0-9Xx]", message = "身份证号须为 18 位，末位可为数字或 X") String idNumber
) {
}
