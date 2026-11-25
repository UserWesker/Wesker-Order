package com.wesker.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    * 根据菜品id查询对应套餐id
    * @param dishIds
    * @return
    * */
    @Select("""
        <script>
        SELECT setmeal_id
        FROM setmeal_dish
        WHERE dish_id IN
        <foreach collection="dishIds" item="dishId"
                 open="(" separator="," close=")">
            #{dishId}
        </foreach>
        </script>
        """)
    List<Long> getSetmealDishIds(@Param("dishIds") List<Long> dishIds);
}
