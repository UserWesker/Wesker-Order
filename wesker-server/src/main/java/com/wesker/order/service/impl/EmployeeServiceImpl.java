package com.wesker.order.service.impl;

import com.wesker.order.constant.EmployeeStatusConstant;
import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.dto.EmployeePageQueryDTO;
import com.wesker.order.entity.Employee;
import com.wesker.order.mapper.AdminAccountMapper;
import com.wesker.order.mapper.EmployeeMapper;
import com.wesker.order.result.PageResult;
import com.wesker.order.service.EmployeeService;
import com.wesker.order.vo.EmployeeVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final AdminAccountMapper adminAccountMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public EmployeeServiceImpl(EmployeeMapper employeeMapper, AdminAccountMapper adminAccountMapper) {
        this.employeeMapper = employeeMapper;
        this.adminAccountMapper = adminAccountMapper;
    }

    @Override
    @Transactional
    public void save(EmployeeDTO employeeDTO, long operatorId) {
        if (employeeDTO.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "初始密码不能超过 72 个 UTF-8 字节");
        }

        if (!EmployeeStatusConstant.ENABLE.equals(adminAccountMapper.lockStatusById(operatorId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号已锁定或不存在");
        }
        if (adminAccountMapper.getByUsername(employeeDTO.username()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该账号已被管理员使用，请更换员工账号");
        }

        LocalDateTime now = LocalDateTime.now();
        Employee employee = new Employee(
                null, employeeDTO.name(), employeeDTO.username(),
                passwordEncoder.encode(employeeDTO.password()),
                employeeDTO.phone(), employeeDTO.sex(), employeeDTO.idNumber(),
                EmployeeStatusConstant.ENABLE, now, now, operatorId, operatorId
        );

        employeeMapper.insert(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        String name = employeePageQueryDTO.name() == null ? "" : employeePageQueryDTO.name().strip();
        long offset = (employeePageQueryDTO.page() - 1L) * employeePageQueryDTO.pageSize();
        long total = employeeMapper.countByName(name);
        List<EmployeeVO> records = offset < total
                ? employeeMapper.findPage(name, employeePageQueryDTO.pageSize(), offset)
                : List.of();
        return new PageResult<>(total, records);
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long employeeId, Long operatorId) {
        if (!EmployeeStatusConstant.ENABLE.equals(adminAccountMapper.lockStatusById(operatorId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号已锁定或不存在");
        }

        int updated = employeeMapper.updateStatus(employeeId, status, LocalDateTime.now(), operatorId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "员工不存在");
        }
    }
}
