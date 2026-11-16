package com.wesker.order.service;

import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.dto.EmployeePageQueryDTO;
import com.wesker.order.entity.Employee;
import com.wesker.order.result.PageResult;
import com.wesker.order.vo.EmployeeVO;
import jakarta.validation.Valid;

public interface EmployeeService {

    //新增员工
    void save(@Valid EmployeeDTO employeeDTO, long operatorId);

    //管理员端员工分页查询
    PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    //管理员端员工状态修改
    void startOrStop(Integer status, Long employeeId, Long operatorId);

    //通过id查询员工信息
    Employee getEmployeeById(Long employeeId);

    //编辑员工信息
    void updateEmployee(Long employeeId, EmployeeDTO employeeDTO, Long operatorId);

    //删除员工信息
    void deleteEmployee(Long employeeId);
}
