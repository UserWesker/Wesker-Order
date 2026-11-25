package com.wesker.order.controller.admin;

import com.wesker.order.constant.MessageConstant;
import com.wesker.order.dto.CategoryDTO;
import com.wesker.order.dto.CategoryPageQueryDTO;
import com.wesker.order.entity.Category;
import com.wesker.order.interceptor.JwtTokenAdminInterceptor;
import com.wesker.order.result.PageResult;
import com.wesker.order.result.Result;
import com.wesker.order.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody CategoryDTO category,
                             @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) long operatorId) {
        categoryService.save(category, operatorId);
        return Result.success(null);
    }

    @GetMapping("/page")
    public Result<PageResult<Category>> page(@Valid @ModelAttribute CategoryPageQueryDTO query) {
        return Result.success(categoryService.categoryPageQuery(query));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") long id, @Valid @RequestBody CategoryDTO category,
                               @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) long operatorId) {
        categoryService.update(id, category, operatorId);
        return Result.success(null);
    }

    @PutMapping("/status/{status:[01]}")
    public Result<Void> startOrStop(@PathVariable("status") int status, @RequestParam("id") long id,
                                    @RequestAttribute(JwtTokenAdminInterceptor.ADMIN_ID_ATTRIBUTE) long operatorId) {
        categoryService.startOrStop(id, status, operatorId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") long id) {
        categoryService.deleteById(id);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam("type") @Min(1) @Max(2) int type) {
        return Result.success(categoryService.listByType(type));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateName() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleCategoryError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Result.error(exception.getReason()));
    }
}
