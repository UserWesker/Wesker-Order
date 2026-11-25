package com.wesker.order.service.impl;

import com.wesker.order.constant.MessageConstant;
import com.wesker.order.dto.CategoryDTO;
import com.wesker.order.dto.CategoryPageQueryDTO;
import com.wesker.order.entity.Category;
import com.wesker.order.mapper.CategoryMapper;
import com.wesker.order.mapper.DishMapper;
import com.wesker.order.result.PageResult;
import com.wesker.order.service.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, DishMapper dishMapper) {
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
    }

    // 新增分类，状态默认禁用
    @Override
    @Transactional
    public void save(CategoryDTO category, long operatorId) {
        if (category.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstant.CATEGORY_TYPE_REQUIRED);
        }
        LocalDateTime now = LocalDateTime.now();
        categoryMapper.insert(new Category(null, category.name(), category.type(), 0,
                now, now, operatorId, operatorId));
    }

    // 按名称和分类类型分页
    @Override
    @Transactional(readOnly = true)
    public PageResult<Category> categoryPageQuery(CategoryPageQueryDTO query) {
        String name = query.name() == null ? "" : query.name().strip();
        long offset = (query.page() - 1L) * query.pageSize();
        long total = categoryMapper.count(name, query.type());
        List<Category> records = offset < total
                ? categoryMapper.findPage(name, query.type(), query.pageSize(), offset)
                : List.of();
        return new PageResult<>(total, records);
    }

    // 修改分类名称
    @Override
    @Transactional
    public void update(long id, CategoryDTO category, long operatorId) {
        requireCategory(categoryMapper.update(id, category.name(), LocalDateTime.now(), operatorId));
    }

    // 切换分类状态
    @Override
    @Transactional
    public void startOrStop(long id, int status, long operatorId) {
        requireCategory(categoryMapper.updateStatus(id, status, LocalDateTime.now(), operatorId));
    }

    // 检查菜品和套餐的引用后再删除分类。
    @Override
    @Transactional
    public void deleteById(long id) {
        if (categoryMapper.lockById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MessageConstant.CATEGORY_NOT_FOUND);
        }
        if (dishMapper.countByCategoryId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        if (categoryMapper.hasSetmealTable() && categoryMapper.hasSetmealByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        try {
            categoryMapper.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MessageConstant.CATEGORY_HAS_REFERENCES, exception);
        }
    }

    // 查询指定类型下可供选择的分类
    @Override
    public List<Category> listByType(int type) {
        return categoryMapper.findEnabledByType(type);
    }

    // 更新未命中记录时，提示分类不存在。
    private void requireCategory(int updatedRows) {
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MessageConstant.CATEGORY_NOT_FOUND);
        }
    }
}
