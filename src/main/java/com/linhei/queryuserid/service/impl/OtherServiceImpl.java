package com.linhei.queryuserid.service.impl;

import com.linhei.queryuserid.service.OtherService;
import com.linhei.queryuserid.utils.FileUtilForReadWrite;
import com.linhei.queryuserid.utils.RedisUtil;
import com.linhei.queryuserid.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author linhei
 */
@Service
@Slf4j
public class OtherServiceImpl implements OtherService {

    final Util util = new Util();
    @Autowired
    RedisUtil redisUtil;

    /**
     * 签到用定时执行脚本
     *
     * @param url           链接
     * @param cookie        cookie
     * @param authorization 认证
     * @return 签到结果
     */
    @Override
    public String requestLink(String url, String cookie, String authorization) {
        String res = null;

        // 调用Utils工具类的request方法
        try {
            res = util.request(url, cookie, authorization);
        } catch (IOException e) {
            log.warn("签到出错：\n" + e);
        }

        return res;
    }

    @Override
    public HashMap<String, ArrayList<String>> getUserData(ArrayList<String> textList, String reg, HashMap<String, String> target) {
//        return util.regex(textList, reg, target);
        return null;
    }

    @Override
    public void register(String url, String authorization) {
        String post = "{\"password\":\"Lin123456!\", \"account\":\"10028\"}";
        try {
            // 执行登录方法
            String register = util.register(url, post, authorization);
            // 获取token的正则
            String token = util.regex(register, "\"code\":\\d,\"token\":\"(\\w+)\",\"uid\":\\d+");
            // 去除所有空格
            token = token.replace(" ", "");
            // 将redis中的token更新
            redisUtil.set("token", token);
            // 执行签到方法
            String s = requestLink(url, token, authorization);
            // 将签到结果记录日志
            FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//255sign.txt", "day:" + s, true);
        } catch (IOException e) {
            log.warn("登录错误：" + e);
        }
    }


}
