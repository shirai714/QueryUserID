package com.linhei.queryuserid.service.impl;

import com.linhei.queryuserid.service.OtherService;
import com.linhei.queryuserid.utils.Util;
import lombok.extern.slf4j.Slf4j;
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
        return util.regex(textList, reg, target);
    }


}
