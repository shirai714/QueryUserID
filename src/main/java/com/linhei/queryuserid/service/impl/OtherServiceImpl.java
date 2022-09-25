package com.linhei.queryuserid.service.impl;

import com.linhei.queryuserid.service.OtherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author linhei
 */
@Service
@Slf4j
public class OtherServiceImpl implements OtherService {

    /**
     * 签到用定时执行脚本
     *
     * @param url           链接
     * @param cookie        cookie
     * @param authorization 认证
     * @return 返回的信息
     */
    @Override
    public String requestLink(String url, String cookie, String authorization) {
        BufferedReader bufIn = null;
        try {
            URL urlC = new URL(url);
            URLConnection urlConn = urlC.openConnection();
            //设置请求属性，有部分网站不加这句话会抛出IOException: Server returned HTTP response code: 403 for URL异常
            //如：b站
            urlConn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");

            urlConn.addRequestProperty("cookie", cookie);
            if (url.contains("255")) {
                urlConn.addRequestProperty("Referer", "https://2550505.com/");
                urlConn.addRequestProperty("Referrer-Policy", "strict-origin-when-cross-origin");
                urlConn.addRequestProperty("authorization", authorization);
                urlConn.addRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
            }
            bufIn = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        } catch (IOException e) {
            log.warn("requestLink实现方法域名请求warn:" + e.getMessage());

        }

        try {
            assert bufIn != null;
            return bufIn.readLine();
        } catch (IOException e) {
            log.warn("requestLink实现方法bufIn warn:" + e.getMessage());
        }


        return url;
    }
}
