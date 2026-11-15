package com.wesker.order.service;

import com.wesker.order.constant.AccountRole;
import com.wesker.order.constant.EmployeeStatusConstant;
import com.wesker.order.dto.EmployeeLoginDTO;
import com.wesker.order.entity.AdminAccount;
import com.wesker.order.entity.Employee;
import com.wesker.order.mapper.AdminAccountMapper;
import com.wesker.order.mapper.EmployeeMapper;
import com.wesker.order.vo.LoginAccount;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final AdminAccountMapper adminAccountMapper;
    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(AdminAccountMapper adminAccountMapper, EmployeeMapper employeeMapper) {
        this.adminAccountMapper = adminAccountMapper;
        this.employeeMapper = employeeMapper;
    }

    public Optional<LoginAccount> login(EmployeeLoginDTO request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return Optional.empty();
        }

        AdminAccount admin = adminAccountMapper.getByUsername(request.username());
        if (admin != null) {
            // 已存在的管理员账号验证失败时，不回退到员工账号，避免混淆两种身份。
            if (!matches(request.password(), admin.password(), admin.status())) {
                return Optional.empty();
            }
            return Optional.of(new LoginAccount(admin.id(), admin.username(), admin.name(), AccountRole.ADMIN));
        }

        Employee employee = employeeMapper.getByUsername(request.username());
        if (employee == null || !matches(request.password(), employee.password(), employee.status())) {
            return Optional.empty();
        }
        return Optional.of(new LoginAccount(employee.id(), employee.username(), employee.name(), AccountRole.EMPLOYEE));
    }

    private boolean matches(String rawPassword, String encodedPassword, Integer status) {
        return EmployeeStatusConstant.ENABLE.equals(status)
                && encodedPassword != null
                && passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
