package com.wesker.order.service.impl;

import com.wesker.order.constant.EmployeeStatusConstant;
import com.wesker.order.constant.MessageConstant;
import com.wesker.order.dto.DishDTO;
import com.wesker.order.dto.DishPageQueryDTO;
import com.wesker.order.entity.Dish;
import com.wesker.order.entity.DishFlavor;
import com.wesker.order.mapper.CategoryMapper;
import com.wesker.order.mapper.DishFlavorMapper;
import com.wesker.order.mapper.DishMapper;
import com.wesker.order.mapper.DishMapper.DishPageRow;
import com.wesker.order.mapper.SetmealDishMapper;
import com.wesker.order.result.PageResult;
import com.wesker.order.service.DishService;
import com.wesker.order.utils.AliOssUtils;
import com.wesker.order.utils.AliOssUtils.ImageUrls;
import com.wesker.order.vo.DishListVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DishServiceImpl implements DishService {

    private static final Logger log = LoggerFactory.getLogger(DishServiceImpl.class);
    private static final Pattern DISH_IMAGE_KEY = Pattern.compile("dishes/[A-Za-z0-9_-]+[.](jpg|jpeg|png)");
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final CategoryMapper categoryMapper;
    private final AliOssUtils aliOssUtils;
    private final SetmealDishMapper setmealDishMapper;

    public DishServiceImpl(DishMapper dishMapper, DishFlavorMapper dishFlavorMapper,
                           CategoryMapper categoryMapper, AliOssUtils aliOssUtils, SetmealDishMapper setmealDishMapper) {
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.categoryMapper = categoryMapper;
        this.aliOssUtils = aliOssUtils;
        this.setmealDishMapper = setmealDishMapper;
    }

    // 关键词匹配菜品名或分类名，按条件分页返回菜品及图片链接。
    @Override
    @Transactional(readOnly = true)
    public PageResult<DishListVO> pageQuery(DishPageQueryDTO query) {
        String keyword = query.name() == null ? "" : query.name().trim();
        long offset = (query.page() - 1L) * query.pageSize();
        long total = dishMapper.count(keyword, query.categoryId(), query.status());
        if (offset >= total) {
            return new PageResult<>(total, List.of());
        }

        List<DishPageRow> rows = dishMapper.findPage(
                keyword, query.categoryId(), query.status(), query.pageSize(), offset);
        Map<String, ImageUrls> imageUrls = findImageUrls(rows);
        List<DishListVO> records = rows.stream()
                .map(row -> {
                    ImageUrls image = imageUrls.get(row.imageKey());
                    return new DishListVO(row.id(), row.name(), row.categoryName(), row.price(), row.status(),
                            row.updateTime(), image == null ? null : image.original(),
                            image == null ? null : image.thumbnail());
                })
                .toList();
        return new PageResult<>(total, records);
    }

    // 为当前页的菜品生成 OSS 临时链接，签名失败时仍返回菜品列表。
    private Map<String, ImageUrls> findImageUrls(List<DishPageRow> rows) {
        List<String> imageKeys = rows.stream()
                .map(DishPageRow::imageKey)
                .filter(key -> key != null && DISH_IMAGE_KEY.matcher(key).matches())
                .distinct()
                .toList();
        if (imageKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return aliOssUtils.generateImageUrls(imageKeys);
        } catch (IllegalStateException exception) {
            log.warn("菜品图片临时链接生成失败，仍返回菜品列表");
            return Collections.emptyMap();
        }
    }

    // 新增菜品，在同一个事务内保存菜品基本信息和对应的口味。
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO, long operatorId) {
        if (categoryMapper.lockEnabledDishCategory(dishDTO.categoryId()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstant.ENABLED_DISH_CATEGORY_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        int status = dishDTO.status() == null ? 0 : dishDTO.status();
        Dish dish = new Dish(null, dishDTO.categoryId(), dishDTO.name(), dishDTO.price(),
                dishDTO.imageKey(), dishDTO.description(), status, now, now, operatorId, operatorId);
        dishMapper.insert(dish);

        if (!dishDTO.flavors().isEmpty()) {
            // 插入菜品后从当前事务连接读取自增主键，再关联到每组口味。
            long dishId = dishMapper.lastInsertedId();
            List<DishFlavor> flavors = dishDTO.flavors().stream()
                    .map(flavor -> new DishFlavor(null, dishId, flavor.name(), flavor.value()))
                    .toList();
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    // 按 ID 更新菜品状态，并记录操作时间和操作人。
    @Override
    @Transactional
    public void startOrStop(int status, long dishId, long operatorId) {
        int updated = dishMapper.updateStatus(dishId, status, LocalDateTime.now(), operatorId);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MessageConstant.DISH_NOT_FOUND);
        }
    }

    // 批量删除菜品，全部检查通过后先删口味再删菜品。
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MessageConstant.DISH_DELETE_SELECTION_REQUIRED);
        }

        // 统一加锁顺序，避免检查后菜品又被启用。
        List<Long> dishIds = ids.stream().distinct().sorted().toList();
        for (Long id : dishIds) {
            Dish dish = dishMapper.getById(id);
            if (dish == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, MessageConstant.DISH_NOT_FOUND);
            }
            if (EmployeeStatusConstant.ENABLE.equals(dish.status())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, MessageConstant.DISH_ON_SALE);
            }
        }

        if (!setmealDishMapper.getSetmealDishIds(dishIds).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : dishIds) {
            dishFlavorMapper.deleteByDishId(id);
            dishMapper.deleteById(id);
        }
    }
}
