package com.wesker.order.mapper;

import com.wesker.order.entity.Dish;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DishMapper {

    String PAGE_FILTER = """
            FROM dish d JOIN category c ON c.id = d.category_id
            WHERE (#{keyword} = '' OR LOCATE(#{keyword}, d.name) > 0 OR LOCATE(#{keyword}, c.name) > 0)
              AND (#{categoryId,jdbcType=BIGINT} IS NULL OR d.category_id = #{categoryId,jdbcType=BIGINT})
              AND (#{status,jdbcType=INTEGER} IS NULL OR d.status = #{status,jdbcType=INTEGER})
            """;

    /*
    * 根据分类id查询菜品数量
    * @param categoryId
    * @return
    * */
    @Select("SELECT COUNT(id) FROM dish WHERE category_id = #{categoryId}")
    Integer countByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT COUNT(*) " + PAGE_FILTER)
    long count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("status") Integer status);

    @Select("""
            SELECT d.id, d.name, c.name AS categoryName, d.price, d.status,
                   d.update_time AS updateTime, d.image_key AS imageKey
            """ + PAGE_FILTER + """
            ORDER BY d.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<DishPageRow> findPage(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                               @Param("status") Integer status, @Param("pageSize") int pageSize,
                               @Param("offset") long offset);

    record DishPageRow(Long id, String name, String categoryName, BigDecimal price,
                       Integer status, LocalDateTime updateTime, String imageKey) {
    }

    @Insert("""
            INSERT INTO dish (
                category_id, name, price, image_key, description, status,
                create_time, update_time, create_user, update_user
            )
            VALUES (
                #{categoryId}, #{name}, #{price}, #{imageKey}, #{description,jdbcType=VARCHAR}, #{status},
                #{createTime}, #{updateTime}, #{createUser}, #{updateUser}
            )
            """)
    void insert(Dish dish);

    @Update("""
            UPDATE dish
            SET status = #{status}, update_time = #{updateTime}, update_user = #{operatorId}
            WHERE id = #{dishId}
            """)
    int updateStatus(@Param("dishId") long dishId, @Param("status") int status,
                     @Param("updateTime") LocalDateTime updateTime, @Param("operatorId") long operatorId);

    @Select("SELECT LAST_INSERT_ID()")
    @Options(useCache = false)
    long lastInsertedId();

    /*
    * 根据主键查询并锁定菜品
    * @param id
    * @return
    */
    @Select("SELECT * FROM dish WHERE id = #{id} FOR UPDATE")
    Dish getById(long id);

    /*
    * 根据主键删除菜品数据
    * @param id
    * @return
    * */
    @Delete("DELETE FROM dish WHERE id = #{id}")
    void deleteById(long id);
}
