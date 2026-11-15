package com.wesker.order.controller.admin;

import com.wesker.order.config.JwtProperties;
import com.wesker.order.constant.AccountRole;
import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.dto.EmployeeLoginDTO;
import com.wesker.order.dto.EmployeePageQueryDTO;
import com.wesker.order.interceptor.JwtTokenAdminInterceptor;
import com.wesker.order.result.PageResult;
import com.wesker.order.result.Result;
import com.wesker.order.service.AuthenticationService;
import com.wesker.order.service.EmployeeService;
import com.wesker.order.utils.JwtUtil;
import com.wesker.order.vo.EmployeeLoginVO;
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

    @PostMapping("/login")
    public ResponseEntity<Result<EmployeeLoginVO>> login(@Valid @RequestBody EmployeeLoginDTO request) {
        var account = authenticationService.login(request).orElse(null);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error("用户名或密码错误，或账号已锁定"));
        }

        String token = JwtUtil.createJWT(
                jwtProperties.adminSecretKey(), jwtProperties.adminTtl(), account.id(), account.role());
        var response = new EmployeeLoginVO(account.id(), account.username(), account.name(), token, account.role());
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }

    //仅管理员可以新增员工
    @PostMapping
    public ResponseEntity<Result<Void>> save(
            @RequestAttribute(JwtTokenAdminInterceptor.ACCOUNT_ID_ATTRIBUTE) Long operatorId,
            @RequestAttribute(JwtTokenAdminInterceptor.ROLE_ATTRIBUTE) String role,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        if (!AccountRole.ADMIN.equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("只有管理员可以新增员工"));
        }

        try {
            employeeService.save(employeeDTO, operatorId);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error("用户名已存在"));
        } catch (ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(Result.error(exception.getReason()));
        }
        return ResponseEntity.ok(Result.success(null));
    }

    //仅管理员查询员工
    @GetMapping("/page")
    public Result<PageResult<EmployeeVO>> page(
            @RequestAttribute(JwtTokenAdminInterceptor.ROLE_ATTRIBUTE) String role,
            @Valid @ModelAttribute EmployeePageQueryDTO query) {
        if (!AccountRole.ADMIN.equals(role)) {
            return Result.error("只有管理员可以查询员工");
        }
        return Result.success(employeeService.pageQuery(query));
    }

    //仅管理员可以修改员工状态
    @PutMapping("/status/{status:[01]}")
    public Result<Void> startOrStop(
            @RequestAttribute(JwtTokenAdminInterceptor.ROLE_ATTRIBUTE) String role,
            @RequestAttribute(JwtTokenAdminInterceptor.ACCOUNT_ID_ATTRIBUTE) Long operatorId,
            @PathVariable("status") Integer status, @RequestParam("id") Long employeeId) {
        if (!AccountRole.ADMIN.equals(role)) {
            return Result.error("只有管理员可以修改员工状态");
        }
        try {
            employeeService.startOrStop(status, employeeId, operatorId);
        } catch (ResponseStatusException exception) {
            return Result.error(exception.getReason());
        }
        return Result.success(null);
    }
}
