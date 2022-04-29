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
     * @param user 用户id处理后的十六进制值
     * @return userList
     */
    @ResponseBody
    @RequestMapping(value = "getUid", method = RequestMethod.GET)
    public List<User> queryByHex(User user, HttpServletRequest request) throws IOException {
        return queryService.queryUID(user, request);
    }

    /**
     * list()方法
     *
     * @param tableName 表名
     * @return 所有结果
     * @throws IOException IO异常
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<User> list(String tableName, String start, String length, HttpServletRequest request) throws IOException {
        return queryService.queryUserList(tableName, start, length, request);
    }

    /**
     * update()
     *
     * @param user 用户对象
     * @return 修改结果
     */
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public String update(User user, HttpServletRequest request) {
        return queryService.update(user, request) ? "修改信息成功" : "修改信息失败";
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public User getBiliUsername(long id, HttpServletRequest request) throws MalformedURLException {
        return queryService.getBiliUsername(id, request);
    }


    @RequestMapping(value = "/insert/insert", method = RequestMethod.POST)
    @ResponseBody
    public String insert(User user) {
        return queryService.insertUser(user) ? "导入成功" : "导入失败";

    }


}

