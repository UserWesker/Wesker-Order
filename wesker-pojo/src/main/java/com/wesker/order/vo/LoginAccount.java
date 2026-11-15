package com.wesker.order.vo;

// 登录身份只包含公开账号资料；不把管理员转换为员工，也不携带密码。
public record LoginAccount(Long id, String username, String name, String role) {
}
