package com.linhei.queryuserid.service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author linhei
 */
public interface OtherService {

    /**
     * 签到用定时执行脚本
     *
     * @param url           链接
     * @param cookie        cookie
     * @param authorization 认证
     * @return 签到结果
     */
    String requestLink(String url, String cookie, String authorization);


    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @param target   目标值
     * @return 过滤后的HashMap
     */
    HashMap<String, ArrayList<String>> getUserData(ArrayList<String> textList, String reg, HashMap<String, String> target);
}
