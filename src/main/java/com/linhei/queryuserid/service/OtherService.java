package com.linhei.queryuserid.service;

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
     * @return 返回的信息
     */
    String requestLink(String url, String cookie, String authorization);
}
