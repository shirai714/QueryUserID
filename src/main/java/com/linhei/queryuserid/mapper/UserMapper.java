package com.linhei.queryuserid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linhei.queryuserid.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> queryUID(User user);

    List<User> queryUserList(@Param("tableName") String tableName, @Param("start") String start, @Param("length") String length);

    boolean update(User user);

    Integer getTableCount(@Param("tableName") String tableName);

//    Integer selectCount(String tableName);
}
