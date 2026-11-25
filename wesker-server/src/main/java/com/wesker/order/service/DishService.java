package com.wesker.order.service;

import com.wesker.order.dto.DishDTO;
import com.wesker.order.dto.DishPageQueryDTO;
import com.wesker.order.result.PageResult;
import com.wesker.order.vo.DishListVO;

import java.util.List;

public interface DishService {

    // 新增菜品和对应口味。
    void saveWithFlavor(DishDTO dishDTO, long operatorId);

    // 按筛选条件分页查询菜品，返回总条数和当前页记录。
    PageResult<DishListVO> pageQuery(DishPageQueryDTO query);

    // 启用或禁用指定菜品。
    void startOrStop(int status, long dishId, long operatorId);

    //菜品批量删除
    void deleteBatch(List<Long> ids);
}
