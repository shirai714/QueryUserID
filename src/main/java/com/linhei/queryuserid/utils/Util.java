package com.linhei.queryuserid.utils;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * @author linhei
 */
@Slf4j
public class Util {

    @Autowired
    QueryService queryService;

    /**
     * 爬取BiliBli的API的方法
     *
     * @param user 需要查询的信息
     * @param id   用户id
     * @return 查询结果 若用户不存在则从数据库中删除该用户
     */
    public User getBiliApi(User user, Long id, String reg, String textUrl) {

        BufferedReader bufIn = null;

        try {
            URL url = new URL(textUrl + id);
            URLConnection urlConn = url.openConnection();
            //设置请求属性，有部分网站不加这句话会抛出IOException: Server returned HTTP response code: 403 for URL异常
            //如：b站
            urlConn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");

            bufIn = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("getBiliUsername\tURLConnection", e);
        }

        String line = "";
        StringBuilder textStr = new StringBuilder();
        while (line != null) {
            //将10行内容追加到textStr字符串中
            for (int i = 0; i < 10; i++) {
                try {
                    assert bufIn != null;
                    line = bufIn.readLine();

                    if (line != null) {
                        textStr.append(line);
                    }
                } catch (IOException e) {
                    log.warn("getBiliUsername\t空指针异常", e);
                }
            }
            //将img标签正则封装对象再调用matcher方法获取一个Matcher对象
            final Matcher matcher = Pattern.compile(reg).matcher(textStr.toString());
            //查找匹配文本
            if (matcher.find()) {
                // 将匹配结果添加到user1中
                user.setName(matcher.group(1));
            } else {
                if ("{\"code\":-412,\"message\":\"请求被拦截\",\"ttl\":1,\"data\":null}".equals(textStr.toString())) {
                    user = getBiliApi(user, id, "[\\W|\\w]+<title>([\\w|\\W]{1,16})的个人空间_哔哩哔哩_Bili", "https://space.bilibili.com/");
                    return user;
                } else {
                    // 若通过BiliBili API无法匹配信息则返回空 则从数据库中删除该用户
                    System.out.println(user);

                    // 将删除信息写入delete日志
                    FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//deleteLog.txt",
                            "User:\t" + user + "\t删除结果：" + queryService.deleteUser(user), true);
                    return null;
                }
            }
        }

        return user;
    }

    /**
     * 将id转换为crc32加密后的hex
     *
     * @param user 用户
     * @return 用户
     */
    public User getHex(User user) {
        String hex = String.valueOf(user.getId());
        CRC32 crc32 = new CRC32();
        crc32.update(hex.getBytes(StandardCharsets.UTF_8));
        // 10进制转16进制
        String getHex = String.format("%16x", crc32.getValue());
        // 去除空字符
        user.setHex(getHex.trim());
        user.setTableName(getUserTableName(user.getHex()));
        user.setUpdateTime(new Date());
        return user;
    }

    /**
     * @param methodName 方法名
     * @param ip         访问者IP地址
     * @param content    查询的信息
     */
    public void recordLog(String methodName, String ip, String content) {
        String information = methodName + "\tIP=" + ip + "\t查询信息=" + content + "\tTime=" + new Date();

        if ("127.0.0.1".equals(ip)) {
            FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//LocalHostIP//" + methodName + ".txt",
                    information,
                    true);
        } else {
            FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//ClientIP//" + methodName + ".txt",
                    information,
                    true);
        }

    }

    /**
     * 获取表名方法
     *
     * @param hex 用户key
     * @return 表名
     */
    public String getUserTableName(String hex) {

        String tableName;
        try {
            tableName = "user_" + hex.substring(0, 2);
        } catch (Exception e) {
            tableName = "user_" + hex.charAt(0);
            log.info("getUserTableName\t hex=" + hex + "\t单字符", e);
        }
        return tableName;
    }
}
