package com.wesker.order.controller.admin;

import com.wesker.order.constant.MessageConstant;
import com.wesker.order.dto.AdminLoginDTO;
import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.dto.EmployeePageQueryDTO;
import com.wesker.order.entity.Employee;
import com.wesker.order.interceptor.JwtTokenAdminInterceptor;
import com.wesker.order.properties.JwtProperties;
import com.wesker.order.result.PageResult;
import com.wesker.order.result.Result;
import com.wesker.order.service.AuthenticationService;
import com.wesker.order.service.EmployeeService;
import com.wesker.order.utils.JwtUtil;
import com.wesker.order.vo.AdminLoginVO;
import com.wesker.order.vo.EmployeeVO;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtProperties jwtProperties;
    private final AuthenticationService authenticationService;

    public EmployeeController(EmployeeService employeeService, JwtProperties jwtProperties,
                              AuthenticationService authenticationService) {
        this.employeeService = employeeService;
        this.jwtProperties = jwtProperties;
        this.authenticationService = authenticationService;
    }

    //管理员登陆
    @PostMapping("/login")
    public ResponseEntity<Result<AdminLoginVO>> login(@Valid @RequestBody AdminLoginDTO request) {
        var account = authenticationService.login(request).orElse(null);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(MessageConstant.LOGIN_FAILED));
        }

        String token = JwtUtil.createJWT(
                jwtProperties.adminSecretKey(), jwtProperties.adminTtl(), account.id());
        var response = new AdminLoginVO(account.id(), account.username(), account.name(), token);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }

    //管理员可以新增员工
    @PostMapping
    public ResponseEntity<Result<Void>> save(
            @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) Long operatorId,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            employeeService.save(employeeDTO, operatorId);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(MessageConstant.USERNAME_ALREADY_EXISTS));
        } catch (ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(Result.error(exception.getReason()));
        }
        return ResponseEntity.ok(Result.success(null));
    }

    //管理员查询员工
    @GetMapping("/page")
    public Result<PageResult<EmployeeVO>> page(
            @Valid @ModelAttribute EmployeePageQueryDTO query) {
        return Result.success(employeeService.pageQuery(query));
    }

    //管理员可以修改员工状态
    @PutMapping("/status/{status:[01]}")
    public Result<Void> startOrStop(
            @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) Long operatorId,
            @PathVariable("status") Integer status, @RequestParam("id") Long employeeId) {
        try {
            employeeService.startOrStop(status, employeeId, operatorId);
        } catch (ResponseStatusException exception) {
            return Result.error(exception.getReason());
        }
        return Result.success(null);
    }

    //按ID查询员工，回填编辑表单
    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(
            @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) Long operatorId,
            @PathVariable("id") Long employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return Result.error(MessageConstant.EMPLOYEE_NOT_FOUND);
        }
        return Result.success(employee);
    }

    //编辑员工信息
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateEmployee(
            @PathVariable("id") Long employeeId,
            @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) Long operatorId,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            employeeService.updateEmployee(employeeId, employeeDTO, operatorId);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(MessageConstant.USERNAME_ALREADY_EXISTS));
        } catch (ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(Result.error(exception.getReason()));
        }
        return ResponseEntity.ok(Result.success(null));
    }

    //删除员工
    @DeleteMapping("/{id}")
    public Result<Void> deleteEmployee(
            @PathVariable("id") Long employeeId){
        employeeService.deleteEmployee(employeeId);
        return Result.success(null);
    }
}
