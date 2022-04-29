package com.linhei.queryuserid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linhei.queryuserid.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * QueryUID方法
     *
     * @param user 实体类
     * @return 查询结果
     */
    List<User> queryUID(User user);

    /**
     * queryUserList方法
     *
     * @param tableName 表名
     * @param start     开始位置
     * @param length    结束长度
     * @return 结果
     */
    List<User> queryUserList(@Param("tableName") String tableName, @Param("start") String start, @Param("length") String length);

    /**
     * update方法
     *
     * @param user 实体类
     * @return 更新结果
     */
    boolean update(User user);

    /**
     * getTableCount方法
     *
     * @param tableName 表名
     * @return 该表数据总条目
     */
    Integer getTableCount(@Param("tableName") String tableName);

    /**
     * insertUser方法
     *
     * @param user 实体类
     * @return 结果
     */
    boolean insertUser(User user);

//    Integer selectCount(String tableName);
}
