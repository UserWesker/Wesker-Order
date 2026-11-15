package com.wesker.order.mapper;

import com.wesker.order.entity.AdminAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminAccountMapper {

    @Select("SELECT id, username, name, password, status FROM admin_account WHERE username = #{username}")
    AdminAccount getByUsername(@Param("username") String username);

    @Select("SELECT status FROM admin_account WHERE id = #{id} FOR UPDATE")
    Integer lockStatusById(@Param("id") long id);
}
