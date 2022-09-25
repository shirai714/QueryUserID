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
     * @param id      用户id
     * @param request 请求
     * @return 查询结果
     * @throws MalformedURLException 格式异常
     */
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public User getUser(long id, HttpServletRequest request) throws MalformedURLException {
        return queryService.getUser(id, request);
    }


}

