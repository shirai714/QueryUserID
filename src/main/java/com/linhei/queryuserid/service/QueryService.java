//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.linhei.queryuserid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linhei.queryuserid.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface QueryService extends IService<User> {
    List<User> queryUID(User user, HttpServletRequest request) throws IOException;

    /**
     * 查询用户列表方法
     *
     * @param tableName 表名
     * @param start     开始位置
     * @param length    结束长度
     * @param request   请求
     * @return 结果
     * @throws IOException IO异常
     */
    List<User> queryUserList(String tableName, String start, String length, HttpServletRequest request) throws IOException;

    /**
     * 获取表名方法
     *
     * @param hex 用户key
     * @return 表名
     */
    String getUserTableName(String hex);

    /**
     * 更新用户方法
     *
     * @param user    实体类
     * @param request 请求
     * @param key     密钥
     * @return 更新结果
     */
    boolean update(User user, HttpServletRequest request, String key);

    /**
     * 获取用户名方法
     *
     * @param id      用户ID
     * @param request 请求
     * @return 结果
     * @throws MalformedURLException 异常
     */
    User getBiliUsername(long id, HttpServletRequest request) throws MalformedURLException;

    /**
     * getTableCount方法
     *
     * @param tableName 表名
     * @return 该表数据总条目
     */
    Integer getTableCount(String tableName);

    /**
     * insertUser方法
     *
     * @param user 实体类
     * @param key  认证秘钥
     * @return 结果
     */
    boolean insertUser(User user, String key);

}
