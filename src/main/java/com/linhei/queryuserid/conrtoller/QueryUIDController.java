package com.linhei.queryuserid.conrtoller;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
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
        // 判断数据密钥是否为空
        if (key == null) {
            return "密钥为空";

        } else if (!key.equals(this.key)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return queryService.update(user, request) ? "修改信息成功" : "修改信息失败";
    }

    /**
     * 数据导入
     *
     * @param user 用户实体
     * @param key  输入的密钥
     * @return 导入结果
     */
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ResponseBody
    public String insert(User user, String key) {
        // 判断数据密钥是否为空
        if (key == null) {
            return "密钥为空";
        } else if (!key.equals(this.key)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return queryService.insertUser(user) ? "导入成功" : "导入失败";
    }


    /**
     * 创建表方法
     *
     * @param tableName 表名
     * @param key       输入的密钥
     * @return 创建结果
     */
    @RequestMapping(value = "/createTable", method = RequestMethod.GET)
    @ResponseBody
    public String createTable(String tableName, String key) {

        // 判断tableName是否为空
        if ("".equals(tableName)) {
            return "请输入正确的表名";
        }

        // 判断数据密钥是否为空
        if (key == null) {
            return "密钥为空";
        } else if (!key.equals(this.key)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return queryService.createTable("user_" + tableName) == 0 ? "创建成功" : "";
    }


    /**
     * 创建私有全局变量key
     */
    private String key = "";

    @PostConstruct // 项目启动后执行注解
    @Scheduled(cron = "0 0 */8 * * ?") // 设置定时任务注解 每过8小时执行一次
//    @Scheduled(fixedDelay = 5000) // 设置定时任务注解 每五秒执行一次
//    @Scheduled(initialDelay = 1, fixedRate = 5) //第一次延迟1秒后执行，之后按fixedRate的规则每5秒执行一次
    private void getKey() {
        // 获取本次key 对key重写
        key = RandomStringUtils.randomAlphanumeric(8);
        System.out.println(key);
        // 将key写入key.txt文件中 用于校验 flag为false表示覆盖写入
        FileUtils.fileLinesWrite("opt//javaApps//key//key.txt",
                key, false);
    }


}

