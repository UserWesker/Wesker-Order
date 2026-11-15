package com.wesker.order.mapper;

import com.wesker.order.entity.Employee;
import com.wesker.order.vo.EmployeeVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("""
            SELECT id, name, username, password, phone, sex, id_number AS idNumber, status,
                   create_time AS createTime, update_time AS updateTime,
                   create_user AS createUser, update_user AS updateUser
            FROM employee
            WHERE username = #{username}
            """)
    Employee getByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM employee WHERE (#{name} = '' OR LOCATE(#{name}, name) > 0)")
    long countByName(@Param("name") String name);

    @Select("""
            SELECT id, username, name, phone, sex, status, create_time AS createTime, update_time AS updateTime
            FROM employee
            WHERE (#{name} = '' OR LOCATE(#{name}, name) > 0)
            ORDER BY id ASC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<EmployeeVO> findPage(@Param("name") String name, @Param("pageSize") int pageSize,
                              @Param("offset") long offset);

    @Insert("""
            INSERT INTO employee (name, username, password, phone, sex, id_number, status,
                                  create_time, update_time, create_user, update_user)
            VALUES (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status},
                    #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    void insert(Employee employee);

    @Update("""
            UPDATE employee
            SET status = #{status}, update_time = #{updateTime}, update_user = #{operatorId}
            WHERE id = #{employeeId}
            """)
    int updateStatus(@Param("employeeId") Long employeeId, @Param("status") Integer status,
                     @Param("updateTime") LocalDateTime updateTime, @Param("operatorId") Long operatorId);
}
