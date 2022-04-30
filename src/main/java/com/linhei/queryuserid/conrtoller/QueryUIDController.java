package com.linhei.queryuserid.conrtoller;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Controller
//@RestController
@RequestMapping("/user")
public class QueryUIDController {

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
        return queryService.queryUID(user, request);
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
     * update()更新数据
     *
     * @param user    用户对象
     * @param request 请求
     * @param key     密钥
     * @return 修改结果
     */
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public String update(User user, HttpServletRequest request, String key) {
        return queryService.update(user, request, key) ? "修改信息成功" : "修改信息失败";
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
    public User getBiliUsername(long id, HttpServletRequest request) throws MalformedURLException {
        return queryService.getBiliUsername(id, request);
    }


    /**
     * 数据导入
     *
     * @param user 用户实体
     * @param key  输入的密钥
     * @return 导入结果
     */
    @RequestMapping(value = "/insert/insert", method = RequestMethod.POST)
    @ResponseBody
    public String insert(User user, String key) {
        return queryService.insertUser(user, key) ? "导入成功" : "导入失败";
    }


}

