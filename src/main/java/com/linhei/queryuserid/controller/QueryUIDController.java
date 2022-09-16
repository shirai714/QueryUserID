package com.linhei.queryuserid.controller;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.FileUtilForReadWrite;
import com.linhei.queryuserid.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * RedisUtil
     */
    @Autowired
    RedisUtil redisUtil;


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

        } else if (!key.equals(this.keyValue)) {
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
        } else if (!key.equals(this.keyValue)) {
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
        } else if (!key.equals(this.keyValue)) {
            // 判断是否与key是否相同
            return "密钥错误，请确认后重试";
        }
        return queryService.createTable("user_" + tableName) == 0 ? "创建成功" : "";
    }

    /**
     * 查看日志
     *
     * @param path 路径
     * @return 日志
     */
    @RequestMapping(value = "/getLog", method = RequestMethod.GET)
    @ResponseBody
    public String getLog(String path) {
        String s = "client";

        // 使用if判断是否为查看客户端的日志
        if (null != path && !path.contains(s)) {
            String mainPath = "opt//javaApps//log//";
            return getString(mainPath + path);
        } else if (null != path && path.contains(s)) {
            String clientPath = "opt//javaApps//log//ClientIP//";
            return getString(clientPath + path.replaceFirst("client", ""));
        }
        // 若path为空则返回签到日志
        return "BiliSignLog：\n<br/>" +
                getString("opt//javaApps//log//BiliSign.txt") +
                "\n<br/>255SignLog：\n<br/>" +
                getString("opt//javaApps//log//255sign.txt");
    }


    /**
     * 创建私有全局变量key
     */
    private String keyValue = "";

    @PostConstruct // 项目启动后执行注解
    @Scheduled(cron = "0 0 */8 * * ?") // 设置定时任务注解 每过8小时执行一次
//    @Scheduled(fixedDelay = 5000) // 设置定时任务注解 每五秒执行一次
//    @Scheduled(initialDelay = 1, fixedRate = 5) //第一次延迟1秒后执行，之后按fixedRate的规则每5秒执行一次
    private void getKey() {

        // 获取本次key 对key重写
        keyValue = RandomStringUtils.randomAlphanumeric(8);
        // 将key加入redis中  用于校验
        String key = "key";
        redisUtil.set(key, keyValue);
        // 设置键为"key"的过期时间为8小时
        redisUtil.expire(key, 28800L);
    }

    /**
     * 调用签到方法
     */
//    @PostConstruct // 项目启动后执行注解
    @Scheduled(cron = "0 0 8 * * ?") // 设置定时任务注解 每过一天执行一次
    private void signIn() {
        // 255sign
        String cookie255 = getString("opt//javaApps//cookie//255");
        String authorization = getString("opt//javaApps//cookie//255Authorization");
        String result255 = queryService.requestLink("https://2550505.com/sign", cookie255, authorization);
        String day255 = queryService.requestLink("https://2550505.com/sign/days", cookie255, null);
        FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//255sign.txt", "day:" + day255 + "\tresult:" + result255
                , true);
        // biliSign
        String biliCookie = getString("opt//javaApps//cookie//bili");
        String biliResult = queryService.requestLink("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign", biliCookie, null);
        FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//BiliSign.txt", "result: " + biliResult
                , true);
    }

    /**
     * 每月执行补签
     */
    @Scheduled(cron = "0 0 8 1 * ?")//定时任务注解 每月1号执行
    private void signFill() {

        String cookie255 = getString("opt//javaApps//cookie//255");
        String authorization = getString("opt//javaApps//cookie//255Authorization");
        final String reg = "\"code\":\\d+,\"count\":(\\d+)";
        Matcher matcher = Pattern.compile(reg).matcher(queryService.requestLink("https://2550505.com/sign/card", cookie255, authorization).toString());
        String group = null;
        if (matcher.find()) {
            group = matcher.group(1);
        }
        assert group != null;
        // 若补签卡大于1则补签
        for (int i = 0; i < Integer.parseInt(group); i++) {
            String result255 = queryService.requestLink("https://2550505.com/sign/fill", cookie255, authorization);
            FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//255fill.txt", "\tresult:" + result255
                    , true);
        }
    }

    /**
     * 获取cookie
     *
     * @param path cookie路径
     * @return cookie
     */
    private String getString(String path) {
        StringBuilder res = new StringBuilder();
        try {
            // 迭代器迭代本地文件
            LineIterator it = FileUtils.lineIterator(new File(path), "UTF-8");
            while (it.hasNext()) {
                res.append(it.nextLine());
                if (!path.contains("cookie")) {
                    res.append("<br/>");
                }
            }
            it.close();
        } catch (IOException e) {
            log.warn(e.toString());
        }

        return res.toString();
    }

}

