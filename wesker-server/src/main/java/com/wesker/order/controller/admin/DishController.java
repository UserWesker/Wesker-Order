package com.wesker.order.controller.admin;

import com.wesker.order.dto.DishDTO;
import com.wesker.order.dto.DishPageQueryDTO;
import com.wesker.order.interceptor.JwtTokenAdminInterceptor;
import com.wesker.order.result.PageResult;
import com.wesker.order.result.Result;
import com.wesker.order.service.DishService;
import com.wesker.order.vo.DishListVO;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

//菜品管理
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    /**
     * 查询菜品管理列表，返回菜品总数和当前页的菜品（含图片链接）。
     *
     * @param query 页码、每页条数和可选的筛选条件
     * @return 分页查询结果，total 是符合条件的总条数，records 是当前页的数据
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageResult<DishListVO>>> page(@Valid @ModelAttribute DishPageQueryDTO query) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Result.success(dishService.pageQuery(query)));
    }

    //新增菜品
    @PostMapping
    public Result<Void> save(@Valid @RequestBody DishDTO dishDTO,
                             @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) long operatorId) {
        dishService.saveWithFlavor(dishDTO, operatorId);
        return Result.success(null);
    }

    // 按 ID 启用或禁用菜品，1 表示起售，0 表示停售。
    @PutMapping("/status/{status:[01]}")
    public Result<Void> startOrStop(@PathVariable("status") int status, @RequestParam("id") long dishId,
                                    @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) long operatorId) {
        dishService.startOrStop(status, dishId, operatorId);
        return Result.success(null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleDishError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Result.error(exception.getReason()));
    }

    //菜品批量删除
    @DeleteMapping
    public Result<Void> delete(@RequestParam("ids") List<Long> ids) {
        dishService.deleteBatch(ids);
        return Result.success(null);
    }
}
