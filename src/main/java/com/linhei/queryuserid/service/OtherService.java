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
     * @return 签到结果
     */
    String requestLink(String url, String cookie, String authorization);


    /**
     * 登录方法
     *
     * @param url255        链接
     * @param authorization 令牌
     */
    void register(String url255, String authorization);
}
