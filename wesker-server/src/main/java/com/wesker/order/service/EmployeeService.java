package com.wesker.order.service;

import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.dto.EmployeePageQueryDTO;
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
}
