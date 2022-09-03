package com.linhei.queryuserid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linhei.queryuserid.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author linhei
 */
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


    /**
     * 创建表方法
     *
     * @param tableName 表名
     * @return 创建结果
     */
    int createTable(@Param("tableName") String tableName);


    /**
     * 删除用户
     *
     * @param user 实体类
     * @return 结果
     */
    Boolean deleteUser(User user);

    /**
     * 获取表名
     *
     * @return 所有表名
     */
    List<String> getTableList();

}
