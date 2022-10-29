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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linhei
 */
public interface QueryService extends IService<User> {
    /**
     * 查询用户
     *
     * @param user    实体类
     * @param request 请求
     * @return 结果
     * @throws IOException IO异常
     */
    List<User> queryUiD(User user, HttpServletRequest request) throws IOException;

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
    Map<String, Object> queryUserList(String tableName, String start, String length, HttpServletRequest request) throws IOException;


    /**
     * 获取用户名方法
     *
     * @param user    用户
     * @param request 请求
     * @return 结果
     * @throws MalformedURLException 异常
     */
    List<User> getUser(User user, HttpServletRequest request) throws MalformedURLException;

    /**
     * getTableCount方法
     *
     * @param tableName 表名
     * @return 该表数据总条目
     */
    Integer getTableCount(String tableName);

    /**
     * 更新用户方法
     *
     * @param user    实体类
     * @param request 请求
     * @return 更新结果
     */
    boolean update(User user, HttpServletRequest request);

    /**
     * insertUser方法
     *
     * @param user 实体类
     * @return 结果
     */
    boolean insertUser(User user);

    /**
     * 创建表
     *
     * @param tableName 表名
     * @return 创建结果
     */
    Integer createTable(String tableName);

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

    /**
     * 通过弹幕内容获取用户
     * 使用弹幕的发送时间详细确认用户
     *
     * @param bv       BV号
     * @param bChar    弹幕
     * @param timeline 弹幕的发送时间
     * @param request  请求
     * @return 查询出的结果
     */
    HashMap<String, ArrayList<String>> getUserChar(String bv, String bChar, String timeline, HttpServletRequest request);


    /**
     * 根据用户和bv号查询该用户在此投稿发送的弹幕
     *
     * @param bv      bv号
     * @param user    用户
     * @param request 请求
     * @return 查询结果
     */
    HashMap<String, ArrayList<String>> getCharForUser(String bv, User user, HttpServletRequest request);

    /**
     * 获取导航栏标题
     *
     * @param lever 用户级别
     * @return redis内结果
     */
    Object getTitleList(String lever);

}