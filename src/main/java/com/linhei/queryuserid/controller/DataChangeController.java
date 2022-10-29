package com.linhei.queryuserid.controller;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 更新用controller
 *
 * @author linhei
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class DataChangeController {

    /**
     * queryService
     */
    @Autowired
    QueryService queryService;

    /**
     * RedisUtil
     */
    @Autowired
    RedisUtil redisUtil;

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

        } else if (!key.equals(this.keyValue)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return queryService.update(user, request) ? "修改信息成功" : "修改信息失败";
    }

    private final String strTrue = "true";

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
        if (!strTrue.equals(keyVerify(key))) {
            return keyVerify(key);
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

        if (!strTrue.equals(keyVerify(key))) {
            return keyVerify(key);
        }

        return queryService.createTable("user_" + tableName) == 0 ? "创建成功" : "";
    }

    private String keyVerify(String key) {


        // 判断数据密钥是否为空
        if (key == null) {
            return "密钥为空";
        } else if (!key.equals(this.keyValue)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return "true";
    }

    /**
     * 创建私有全局变量key
     */
    private String keyValue = "";

    /*
     * @Scheduled(fixedDelay = 5000) // 设置定时任务注解 每五秒执行一次
     * @Scheduled(initialDelay = 1, fixedRate = 5) //第一次延迟1秒后执行，之后按fixedRate的规则每5秒执行一次
     */
//    @PostConstruct // 项目启动后执行注解
    @Scheduled(cron = "0 0 */8 * * ?") // 设置定时任务注解 每过8小时执行一次
    private void getKey() {

        // 获取本次key 对key重写
        keyValue = RandomStringUtils.randomAlphanumeric(8);
        // 将key加入redis中  用于校验
        String key = "key";
        redisUtil.set(key, keyValue);
        // 设置键为"key"的过期时间为8小时
        redisUtil.expire(key, 28800L);
    }

}
