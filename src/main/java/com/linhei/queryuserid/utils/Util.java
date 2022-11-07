package com.linhei.queryuserid.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.linhei.queryuserid.entity.CharEntity;
import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
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
@Component
public class Util {

    @Autowired
    private QueryService queryService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 爬取BiliBli的API的方法
     *
     * @param user 需要查询的信息
     * @param id   用户id
     * @param reg  正则
     * @return 查询结果 若用户不存在则从数据库中删除该用户
     */
    public User getBiliApi(User user, Object id, String reg) {
        return getBiliApi(user, id, reg, null);
    }

    /**
     * 爬取BiliBli的API的方法
     *
     * @param user    需要查询的信息
     * @param id      用户id
     * @param reg     正则
     * @param textUrl 链接
     * @return 查询结果 若用户不存在则从数据库中删除该用户
     */
    public User getBiliApi(User user, Object id, String reg, String textUrl) {

        if (textUrl == null) textUrl = "https://api.bilibili.com/x/web-interface/card?mid=%s";

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
        user.setUpdateTime(dateFormat(new Date()));

        return user;
    }

    /**
     * 格式化时间
     *
     * @param date date
     * @return date
     */
    public Date dateFormat(Date date) {
        Date parse = null;
        try {
            // 将日期格式化为 yyyy-MM-dd HH:mm:ss
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = simpleDateFormat.format(date);
            parse = simpleDateFormat.parse(dateString);

        } catch (ParseException e) {
            log.warn("日期转换失败：", e);
        }
        return parse;
    }

    /**
     * 将时间戳转换为时间
     *
     * @param timestamp 时间戳
     * @return 时间
     */
    public Date timestampToDate(long timestamp) {

        timestamp *= 1000;
        // 返回结果
        return  new Date(timestamp);

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
     * 登录请求方法
     *
     * @param url           链接
     * @param post          POST内容
     * @param authorization 密钥
     * @return 返回参数
     * @throws IOException IOException
     */
    public String register(String url, JSONObject post, String authorization) throws IOException {

        // 请求头
        HttpHeaders headers = new HttpHeaders();
        // 设定请求字符集 指定返回类型
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        // 设置请求头
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        headers.add("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.add("origin", "https://2550505.com");
        headers.add("referer", "https://2550505.com/login");
        headers.add("content-type", "application/json");
        headers.add("authorization", authorization);

        // 请求实体
        HttpEntity<String> formEntity = new HttpEntity<>(JSON.toJSONString(post), headers);
        // 使用restTemplate发送POST请求
        return restTemplate.postForObject(url, formEntity, String.class);
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
        HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();

        setRequestProperty(urlConnection);

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


        return bufferedReaderToString(bufIn);
    }

    public String bufferedReaderToString(BufferedReader bufIn) throws IOException {
        StringBuilder sb = new StringBuilder();
        String s;

        while ((s = bufIn.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 设置请求头属性
     *
     * @param urlConnection 连接
     */
    public void setRequestProperty(HttpURLConnection urlConnection) {
        // 设置请求头
        urlConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");
        urlConnection.addRequestProperty("Referrer-Policy", "strict-origin-when-cross-origin");
        urlConnection.addRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
    }

    /**
     * 不带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @return 过滤后的HashMap
     */
    public ArrayList<CharEntity> regex(ArrayList<String> textList, String reg) {

        return regex(textList, reg, null);
    }

    /**
     * 不带target的regex方法
     *
     * @param textList 源文件列表
     * @return 过滤后的hashmap
     */
    public ArrayList<CharEntity> regex(ArrayList<String> textList) {
        return regex(textList, (HashMap<String, String>) null);
    }

    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param target   目标值
     * @return 过滤后的HashMap
     */
    public ArrayList<CharEntity> regex(ArrayList<String> textList, HashMap<String, String> target) {
        // 正则表达式
        String reg = "<d p=\"([\\d.\\d|\\d]{1,}),\\d,\\d{1,2},\\d{1,8},(\\d{1,11}),\\d,(\\w{1,10}),\\d{1,20},\\d{1,2}\">([\\W|\\w]{1,100})</d>";
        return regex(textList, reg, target, null);
    }


    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     *                 //     * @param target   目标值
     * @return 过滤后的HashMap
     */
    public ArrayList<CharEntity> regex(ArrayList<String> textList, String reg, HashMap<String, String> target) {
        return regex(textList, reg, null, null);
    }

    int tem = 0;

    /**
     * 带目标且返回批量数据的regex方法
     *
     * @param textList 源文件列表
     * @param reg      正则表达式
     * @param target   目标值
     * @param request  请求
     * @return 过滤后的HashMap
     */
    public ArrayList<CharEntity> regex(ArrayList<String> textList, String reg, HashMap<String, String> target, HttpServletRequest request) {
        int temp = 0;

        // 判断是否target是否为空
        if (target != null && target.containsKey("bChar") && !target.containsKey("timeline")) {
            temp = 1;
        } else if (target != null && target.containsKey("timeline")) {
            temp = 2;
        } else if (target != null && target.containsKey("userKey")) {
            temp = 3;
        }

        // 默认返回对象 hashmap最大容量为8
        HashMap<String, ArrayList<String>> res = new HashMap<>(8);

        this.tem = 0;

        ArrayList<CharEntity> charList = new ArrayList<>();
        int id = 0;
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

                Date timestamp2 = timestampToDate(Long.parseLong(timestamp));
                // 获取组3的数据  用户id
                String userKey = matcher.group(3);
                list.add(String.valueOf(timestamp2));
                // 获取组4的数据  弹幕
                String bChar = matcher.group(4);
                list.add(bChar);


                String targetTimeline;
                // 获取查询目标的弹幕
                boolean flag;
                // 目标char值
                String bCharTarget;

                switch (temp) {
                    case 1:
                        // 若查询目标传入的弹幕存在于获取的弹幕中或与其相等
                        bCharTarget = target.get("bChar");
                        flag = bChar.equals(bCharTarget) || bChar.contains(bCharTarget);
                        if (flag) {
                            // 若符合条件则将其加入CharList中
                            id++;
                            charList.add(new CharEntity(id, userKey, timestamp2, bChar, timeline));

                        }
                        continue;
                    case 2:
                        // 为发送时间赋值
                        targetTimeline = target.get("timeline");
                        // 为目标复制
                        bCharTarget = target.get("bChar");
                        double targetTimelineDouble = Double.parseDouble(targetTimeline);
                        double timelineDouble = Double.parseDouble(timeline);
                        flag = bChar.equals(bCharTarget) || bChar.contains(bCharTarget);
                        // 若查询目标传入的弹幕存在于获取的弹幕中或与其相等 且 查询目标和获取的弹幕在视频中的时间相差±1秒
                        boolean b = flag && (Math.max(timelineDouble, targetTimelineDouble) - Math.min(timelineDouble, targetTimelineDouble) <= 1);
                        if (b) {
                            // 若符合条件则将其加入CharList中
                            id++;
                            charList.add(new CharEntity(id, userKey, timestamp2, bChar, timeline));
                        }
                        continue;
                    case 3:
                        String user = target.get("userKey");
                        if (userKey.equals(user)) {
                            id++;
                            charList.add(new CharEntity(id, userKey, timestamp2, bChar, timeline));
                        }

                        continue;
                    default:
                        id++;
                        charList.add(new CharEntity(id, userKey, timestamp2, bChar, timeline));
                }


            }

        }
        return charList;
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


    /**
     * 弹幕文件保存到本地
     *
     * @param redisHasCid cid用于判断Redis中是否已已存在
     * @param newlineChar 处理后的弹幕文件
     * @param cid         cid
     * @throws IOException IOException
     */
    public void getChars(Object redisHasCid, ArrayList<String> newlineChar, String cid) throws IOException {

        // 弹幕内容保存路径
        File file = new File(String.format("hdd//data//biliChar//%s.xml", cid));

        // 通过查询redis中存储的数据判断是否已有该BV号的弹幕文件
        if (redisHasCid == null) {
            newlineChar = request("https://api.bilibili.com/x/v1/dm/list.so?oid=%s", cid, "</d>", true);


            // 将弹幕文件保存到服务器硬盘
            for (int i = 0; i < newlineChar.size(); i++) {
                FileUtils.writeStringToFile(file, newlineChar.get(i), "UTF-8", i != 0);
            }

        } else {
            // 若存在则直接读取本地文件
            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            while (it.hasNext()) {
                newlineChar.add(it.nextLine());
            }
            it.close();
        }

    }
}
