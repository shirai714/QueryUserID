package com.linhei.queryuserid.controller;

import com.linhei.queryuserid.service.OtherService;
import com.linhei.queryuserid.utils.FileUtilForReadWrite;
import com.linhei.queryuserid.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 查询日志和自动签到用Controller
 *
 * @author linhei
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class LogAndSignController {


    @Autowired
    OtherService otherService;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 查看日志
     *
     * @param path 路径
     * @return 日志
     */
    @RequestMapping(value = "/getLog", method = RequestMethod.GET)
    @ResponseBody
    public String getLog(String path) {
        final String s = "client";

        // 使用if判断是否为查看客户端的日志
        if (null != path && !path.contains(s)) {
            String mainPath = "opt//javaApps//log//";
            return getString(mainPath + path);
        } else if (null != path && path.contains(s)) {
            String clientPath = "opt//javaApps//log//ClientIP//";
            return getString(clientPath + path.replaceFirst(s, ""));
        }
        // 若path为空则返回签到日志
        return "BiliSignLog：\n<br/>" +
                getString("opt//javaApps//log//BiliSign.txt") +
                "\n<br/>255SignLog：\n<br/>" +
                getString("opt//javaApps//log//255sign.txt");
    }

    /**
     * 调用签到方法
     */
    @PostConstruct // 项目启动后执行注解
    @Scheduled(cron = "0 0 8 * * ?") // 设置定时任务注解 每过一天执行一次
    private void signIn() {
        // 255sign
        String url255 = "https://2550505.com/sign";
        String token255 = String.valueOf(redisUtil.get("token"));
        String authorization = String.valueOf(redisUtil.get("authorization"));
        String result255 = otherService.requestLink(url255, token255, authorization);
        // 若登录失效则调用登录方法
        String s = "用户未登陆或登陆已过期,请登录后再试";
        if (result255.contains(s)) {
            otherService.register(url255, authorization);
        }
        String day255 = otherService.requestLink("https://2550505.com/sign/days", token255, null);
        FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//255sign.txt", "day:" + day255 + "\tresult:" + result255
                , true);
        // biliSign
        String biliCookie = getString("opt//javaApps//cookie//bili");
        String biliResult = otherService.requestLink("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign", biliCookie, null);
        FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//BiliSign.txt", "result: " + biliResult
                , true);
    }

    /**
     * 每月执行补签
     */
    @Scheduled(cron = "0 0 8 1 * ?") //定时任务注解 每月1号执行
    private void signFill() {

        String cookie255 = String.valueOf(redisUtil.get("token"));
        String authorization = getString("opt//javaApps//cookie//255Authorization");
        final String reg = "\"code\":\\d+,\"count\":(\\d+)";
        Matcher matcher = Pattern.compile(reg).matcher(otherService.requestLink("https://2550505.com/sign/card", cookie255, authorization));
        String group = null;
        if (matcher.find()) {
            group = matcher.group(1);
        }
        assert group != null;
        // 若补签卡大于1则补签
        for (int i = 0; i < Integer.parseInt(group); i++) {
            String result255 = otherService.requestLink("https://2550505.com/sign/fill", cookie255, authorization);
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
