package com.wesker.order.mapper;

import com.wesker.order.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    @Insert("""
            <script>
            INSERT INTO dish_flavor (dish_id, name, value)
            VALUES
            <foreach collection="flavors" item="flavor" separator=",">
                (#{flavor.dishId}, #{flavor.name}, #{flavor.value})
            </foreach>
            </script>
            """)
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /*
    * 根据菜品id删除对应的口味数据
    * @param dishId
    * */
    @Delete("DELETE FROM dish_flavor WHERE dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
}
