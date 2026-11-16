package com.wesker.order.mapper;

import com.wesker.order.dto.EmployeeDTO;
import com.wesker.order.entity.Employee;
import com.wesker.order.vo.EmployeeVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("SELECT COUNT(*) FROM employee WHERE (#{name} = '' OR LOCATE(#{name}, name) > 0)")
    long countByName(@Param("name") String name);

    //分页查询员工信息
    @Select("""
            SELECT id, username, name, phone, sex, status, create_time AS createTime, update_time AS updateTime
            FROM employee
            WHERE (#{name} = '' OR LOCATE(#{name}, name) > 0)
            ORDER BY id ASC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<EmployeeVO> findPage(@Param("name") String name, @Param("pageSize") int pageSize,
                              @Param("offset") long offset);

    //添加员工
    @Insert("""
            INSERT INTO employee (name, username, phone, sex, id_number, status,
                                  create_time, update_time, create_user, update_user)
            VALUES (#{name}, #{username}, #{phone}, #{sex}, #{idNumber}, #{status},
                    #{createTime}, #{updateTime}, #{createUser}, #{updateUser})
            """)
    void insert(Employee employee);

    //修改员工状态
    @Update("""
            UPDATE employee
            SET status = #{status}, update_time = #{updateTime}, update_user = #{operatorId}
            WHERE id = #{employeeId}
            """)
    int updateStatus(@Param("employeeId") Long employeeId, @Param("status") Integer status,
                     @Param("updateTime") LocalDateTime updateTime, @Param("operatorId") Long operatorId);

    //根据id查询员工信息
    @Select("""
            SELECT id, name, username, phone, sex, id_number AS idNumber, status,
                   create_time AS createTime, update_time AS updateTime,
                   create_user AS createUser, update_user AS updateUser
            FROM employee
            WHERE id = #{id}
            """)
    Employee getEmployeeById(@Param("id") Long employeeId);

    //编辑员工信息
    @Update("""
        UPDATE employee
        SET username = #{dto.username},
            name = #{dto.name},
            phone = #{dto.phone},
            sex = #{dto.sex},
            id_number = #{dto.idNumber},
            update_time = #{updateTime},
            update_user = #{operatorId}
        WHERE id = #{employeeId}
        """)
    int update(@Param("employeeId") Long employeeId,
               @Param("dto") EmployeeDTO employeeDTO,
               @Param("updateTime") LocalDateTime updateTime,
               @Param("operatorId") Long operatorId);

    //删除员工信息
    @Delete("""
        DELETE FROM employee
        WHERE id = #{employeeId}
        """)
    void deleteById(@Param("employeeId") Long employeeId);
}
