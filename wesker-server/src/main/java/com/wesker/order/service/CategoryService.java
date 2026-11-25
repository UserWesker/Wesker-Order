package com.wesker.order.service;

import com.wesker.order.dto.CategoryDTO;
import com.wesker.order.dto.CategoryPageQueryDTO;
import com.wesker.order.entity.Category;
import com.wesker.order.result.PageResult;

import java.util.List;

public interface CategoryService {

    // 新增分类，默认禁用。
    void save(CategoryDTO category, long operatorId);

    // 按分类名称和类型筛选，返回总条数和当前页记录。
    PageResult<Category> categoryPageQuery(CategoryPageQueryDTO query);

    // 按 ID 修改分类名称，并记录操作人。
    void update(long id, CategoryDTO category, long operatorId);

    // 启用或禁用指定分类。
    void startOrStop(long id, int status, long operatorId);

    // 删除没有关联菜品或套餐的分类。
    void deleteById(long id);

    // 查询指定类型下已启用的分类。
    List<Category> listByType(int type);
}
