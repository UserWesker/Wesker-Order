package com.wesker.order.service;

import com.wesker.order.constant.EmployeeStatusConstant;
import com.wesker.order.dto.AdminLoginDTO;
import com.wesker.order.entity.AdminAccount;
import com.wesker.order.mapper.AdminAccountMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final AdminAccountMapper adminAccountMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(AdminAccountMapper adminAccountMapper) {
        this.adminAccountMapper = adminAccountMapper;
    }

    // 校验管理员账号是否启用、密码是否正确，验证通过后返回账号信息。
    public Optional<AdminAccount> login(AdminLoginDTO request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return Optional.empty();
        }

        AdminAccount admin = adminAccountMapper.getByUsername(request.username());
        if (admin == null || !EmployeeStatusConstant.ENABLE.equals(admin.status())
                || !passwordEncoder.matches(request.password(), admin.password())) {
            return Optional.empty();
        }
        return Optional.of(admin);
    }
}
