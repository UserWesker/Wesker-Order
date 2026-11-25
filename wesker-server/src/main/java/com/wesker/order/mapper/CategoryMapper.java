package com.wesker.order.mapper;

import com.wesker.order.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CategoryMapper {

    String COLUMNS = """
            id, name, type, status,
            create_time AS createTime, update_time AS updateTime,
            create_user AS createUser, update_user AS updateUser
            """;

    String FILTER = """
            FROM category
            WHERE (#{name} = '' OR LOCATE(#{name}, name) > 0)
              AND (#{type,jdbcType=INTEGER} IS NULL OR type = #{type,jdbcType=INTEGER})
            """;

    @Insert("""
            INSERT INTO category (name, type, status, create_time, update_time, create_user, update_user)
            VALUES (#{name}, #{type}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    void insert(Category category);

    @Select("SELECT COUNT(*) " + FILTER)
    long count(@Param("name") String name, @Param("type") Integer type);

    @Select("SELECT " + COLUMNS + FILTER + """
            ORDER BY id ASC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<Category> findPage(@Param("name") String name, @Param("type") Integer type,
                            @Param("pageSize") int pageSize, @Param("offset") long offset);

    @Update("""
            UPDATE category
            SET name = #{name}, update_time = #{updateTime}, update_user = #{operatorId}
            WHERE id = #{id}
            """)
    int update(@Param("id") long id, @Param("name") String name,
               @Param("updateTime") LocalDateTime updateTime, @Param("operatorId") long operatorId);

    @Update("""
            UPDATE category
            SET status = #{status}, update_time = #{updateTime}, update_user = #{operatorId}
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") long id, @Param("status") int status,
                     @Param("updateTime") LocalDateTime updateTime, @Param("operatorId") long operatorId);

    @Select("SELECT " + COLUMNS + """
            FROM category
            WHERE type = #{type} AND status = 1
            ORDER BY id ASC
            """)
    List<Category> findEnabledByType(@Param("type") int type);

    @Select("SELECT id FROM category WHERE id = #{id} AND type = 1 AND status = 1 FOR SHARE")
    Long lockEnabledDishCategory(@Param("id") long id);

    @Select("SELECT id FROM category WHERE id = #{id} FOR UPDATE")
    Long lockById(@Param("id") long id);

    @Select("""
            SELECT EXISTS (
                SELECT 1 FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = 'setmeal' AND table_type = 'BASE TABLE'
            )
            """)
    boolean hasSetmealTable();

    @Select("SELECT EXISTS (SELECT 1 FROM setmeal WHERE category_id = #{categoryId})")
    boolean hasSetmealByCategoryId(@Param("categoryId") long categoryId);

    @Delete("DELETE FROM category WHERE id = #{id}")
    void deleteById(@Param("id") long id);
}
