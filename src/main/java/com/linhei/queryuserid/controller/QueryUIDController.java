package com.linhei.queryuserid.controller;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author linhei
 */
@Controller
//@RestController
@RequestMapping("/user")
@Slf4j
public class QueryUIDController {

    /**
     * queryService
     */
    @Autowired
    QueryService queryService;


    /**
     * 根据hex查询用户
     *
     * @param user 用户id处理后的十六进制值
     * @return userList
     */
    @ResponseBody
    @RequestMapping(value = "getUid", method = RequestMethod.GET)
    public List<User> queryByHex(User user, HttpServletRequest request) throws IOException {
        return queryService.queryUiD(user, request);
    }

    /**
     * list()方法 查询表
     *
     * @param tableName 表名
     * @param request   请求
     * @param start     开始索引
     * @param length    结束索引
     * @return 所有结果
     * @throws IOException IO异常
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<User> list(String tableName, String start, String length, HttpServletRequest request) throws IOException {
        return queryService.queryUserList(tableName, start, length, request);
    }


    /**
     * 查询用户
     *
     * @param user    用户
     * @param request 请求
     * @return 查询结果
     * @throws MalformedURLException 格式异常
     */
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public User getUser(User user, HttpServletRequest request) throws MalformedURLException {
        return queryService.getUser(user, request);
    }

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
    @ResponseBody
    @RequestMapping(value = "/getUserForChar", method = RequestMethod.GET)
    public HashMap<String, ArrayList<String>> getUserChar(String bv, String bChar, String timeline, HttpServletRequest request) {
        return queryService.getUserChar(bv, bChar, timeline, request);
    }

    /**
     * 根据用户和bv号查询该用户在此投稿发送的弹幕
     *
     * @param bv      bv号
     * @param user    用户
     * @param request 请求
     * @return 查询结果
     */
    @ResponseBody
    @RequestMapping(value = "/getCharForUser", method = RequestMethod.GET)
    public HashMap<String, ArrayList<String>> getCharForUser(String bv, User user, HttpServletRequest request) {
        return queryService.getCharForUser(bv, user, request);
    }
}

