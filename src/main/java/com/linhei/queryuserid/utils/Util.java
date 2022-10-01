package com.linhei.queryuserid.utils;

import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
    public User getBiliApi(User user, Object id, String reg, String textUrl) {

        String regex = "[\\W|\\w]+<title>([\\w|\\W]{1,16})的个人空间_哔哩哔哩_Bili";
        String userName;
        String code412 = "{\"code\":-412,\"message\":\"请求被拦截\",\"ttl\":1,\"data\":null}";
        try {
            String request = request(textUrl, id);
            if (code412.equals(request)) {
                return getBiliApi(user, id, regex, "https://space.bilibili.com/%s");
            } else if ((userName = regex(request, reg)) != null) {
                user.setName(userName);
            } else {
                // 若通过BiliBili API无法匹配信息则返回空 则从数据库中删除该用户
                log.warn("由于未查询到数据故而从MySQL中删除数据用户：" + user);

                // 将删除信息写入delete日志
                FileUtilForReadWrite.fileLinesWrite("opt//javaApps//log//deleteLog.txt",
                        "User:\t" + user + "\t删除结果：" + queryService.deleteUser(user), true);
                return null;
            }
        } catch (IOException e) {
            log.warn("获取用户名过程中出错：\t" + e);
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
        String local = "127.0.0.1";
        if (local.equals(ip)) {
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

    /**
     * 带tag请求方法
     *
     * @param url   链接
     * @param id    bv或cid
     * @param regex 正则
     * @param tag   是否需要解压
     * @return 处理后的xml文件
     * @throws IOException IO异常
     */
    public ArrayList<String> request(String url, Object id, String regex, Boolean tag) throws IOException {

        String req = request(url, id, tag);
        String[] tem = req.split(regex);
        String s = "</source>";
        String[] split = tem[0].split(s);
        split[0] += s + "\n";
        ArrayList<String> list = new ArrayList<>();
        String[] res = new String[(tem.length + 1)];
        System.arraycopy(split, 0, res, 0, split.length);
        System.arraycopy(tem, 1, res, 2, tem.length - 1);

        for (int i = 0; i < res.length; i++) {
            if (i == 0) {
                list.add(res[i]);
            } else if (i == res.length - 1) {
                list.add(res[i]);
            } else {
                list.add(res[i] + regex + "\n");
            }

        }
        return list;

    }

    /**
     * 不带tag请求方法，tag默认为false
     *
     * @param url 链接
     * @param id  bv或cid
     * @return 未处理String
     * @throws IOException IO异常
     */
    public String request(String url, Object id) throws IOException {
        return request(url, id, false, null, null);
    }

    /**
     * 签到用request
     *
     * @param url           链接
     * @param authorization 授权
     * @param cookie        cookie
     * @return 未处理的String
     * @throws IOException IO异常
     */


    public String request(String url, String cookie, String authorization) throws IOException {
        return request(url, null, false, cookie, authorization);
    }

    /**
     * 带tag的请求方法
     *
     * @param url 链接
     * @param id  bv或cid
     * @param tag 是否需要解压数据
     * @return 未处理的
     */
    public String request(String url, Object id, Boolean tag) throws IOException {
        return request(url, id, tag, null, null);
    }

    /**
     * 主请求方法
     *
     * @param url 链接
     * @param id  bv或cid
     * @param tag 是否需要解压
     * @return 未处理String
     * @throws IOException IO异常
     */
    public String request(String url, Object id, Boolean tag, String cookie, String authorization) throws IOException {

        BufferedReader bufIn;
        URL url1 = new URL(null, String.format(url, id));
        // 打开链接
        URLConnection urlConnection = url1.openConnection();

        // 设置请求头
        urlConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");
        urlConnection.addRequestProperty("Referrer-Policy", "strict-origin-when-cross-origin");
        urlConnection.addRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");

        // 判断是否为签到用request
        if (null != cookie) {
            urlConnection.addRequestProperty("cookie", cookie);
            // 判断是否为255的签到
            if (null != authorization) {
                urlConnection.addRequestProperty("Referer", "https://2550505.com/");
                urlConnection.addRequestProperty("authorization", authorization);
            }
        }

        // 判断是否需要解压数据并忽略zlib头
        if (tag) {
            bufIn = new BufferedReader(new InputStreamReader(new InflaterInputStream((urlConnection.getInputStream()), new Inflater(true))));
        } else {
            bufIn = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        }

        StringBuilder sb = new StringBuilder();
        String s;

        while ((s = bufIn.readLine()) != null) {
            sb.append(s);
        }

        return sb.toString();
    }


    /**
     * 不带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @return 过滤后的HashMap
     */
    public HashMap<String, ArrayList<String>> regex(ArrayList<String> textList, String reg) {

        return regex(textList, reg, null);
    }

    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @param target   目标值
     * @return 过滤后的HashMap
     */
    public HashMap<String, ArrayList<String>> regex(ArrayList<String> textList, String reg, HashMap<String, String> target) {
        return regex(textList, reg, target, null);
    }

    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @param target   目标值
     * @param request  请求
     * @return 过滤后的HashMap
     */
    public HashMap<String, ArrayList<String>> regex(ArrayList<String> textList, String reg, HashMap<String, String> target, HttpServletRequest request) {


        // 默认返回对象 hashmap最大容量为8
        HashMap<String, ArrayList<String>> res = new HashMap<>(8);

        int tem = 0;
        // 遍历
        for (int i = 1; i < textList.size(); i++) {
            // 使用regex过滤
            Matcher matcher = Pattern.compile(reg).matcher(textList.get(i));
            if (matcher.find()) {
                ArrayList<String> list = new ArrayList<>();
                // 获取组1的数据  视频中的时间
                String timeline = matcher.group(1);
                list.add(timeline);
                // 获取组2的数据  时间戳
                String timestamp = matcher.group(2);
                // 调用时间戳转换方法
                timestamp = timestampToDate(Long.parseLong(timestamp));
                // 获取组3的数据  用户id
                String userKey = matcher.group(3);
                list.add(timestamp);
                // 获取组4的数据  弹幕
                String bChar = matcher.group(4);
                list.add(bChar);
                User user = null;
                if (request != null) {
                    try {
                        System.out.println(userKey);
                        user = queryService.getUser(new User(userKey), request);
                        System.out.println(user);

                    } catch (MalformedURLException e) {
                        log.warn("获取用户信息时出现错误：" + e.getCause());
                    }
                    userKey = String.valueOf(user.getId());
                }
                // 判断是否有查询目标
                if (target != null) {
                    String targetTimeline;
                    // 获取查询目标的弹幕
                    String bCharTarget = target.get("bChar");

                    // 若查询目标传入的弹幕存在于获取的弹幕中或与其相等
                    boolean flag = bChar.equals(bCharTarget) || bChar.contains(bCharTarget);
                    // 判断是否有发送时间条件 若有为其赋值
                    if ((targetTimeline = target.get("timeline")) != null) {

                        double targetTimelineDouble = Double.parseDouble(targetTimeline);
                        double timelineDouble = Double.parseDouble(timeline);
                        // 若查询目标传入的弹幕存在于获取的弹幕中或与其相等 且 查询目标和获取的弹幕在视频中的时间相差±3秒
                        boolean b = flag && (Math.max(timelineDouble, targetTimelineDouble) - Math.min(timelineDouble, targetTimelineDouble) <= 3);
                        if (b) {
                            // 符合上述条件直接返回本次的循环的数据
                            HashMap<String, ArrayList<String>> temp = new HashMap<>(textList.size());
                            if (user != null) list.add(user.getName());
                            temp.put(userKey, list);
                            return temp;
                        }
                    } else {
                        if (flag) {
                            if (res.containsKey(userKey)) {
                                ArrayList<String> strings = res.get(userKey);
                                strings.addAll(list);
                                if (user != null) strings.add(user.getName());
                                res.put(userKey, strings);

                            } else {
                                if (user != null) list.add(user.getName());
                                res.put(userKey, list);
                                tem++;
                                if (tem > 7) {
                                    res.put("结果超过8次请输入弹幕所在时间用于详细确认", null);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // 若没有目标则判断userID是否存在hashmap中 若存在则将本次匹配的结果加入相符的userID中
                    if (res.containsKey(userKey)) {
                        ArrayList<String> strings = res.get(userKey);
                        strings.addAll(list);
                        if (user != null) strings.add(user.getName());
                        res.put(userKey, strings);

                    } else {
                        // 否则直接以userID为key加入hashmap中
                        if (user != null) list.add(user.getName());
                        res.put(userKey, list);
                    }
                }
            }

        }
        return res;
    }


    /**
     * 不带目标的regex方法
     *
     * @param text  元数据
     * @param regex 正则公式
     * @return 相匹配的结果
     */
    public String regex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String timestampToDate(long timestamp) {

        timestamp *= 1000;
        // 格式化
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        date.setTime(timestamp);
        // 返回结果
        return simpleDateFormat.format(date);

    }
}
