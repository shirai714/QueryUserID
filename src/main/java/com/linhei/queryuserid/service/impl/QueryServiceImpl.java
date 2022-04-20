package com.linhei.queryuserid.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.mapper.UserMapper;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.FileUtils;
import com.linhei.queryuserid.utils.IpUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

@Service
public class QueryServiceImpl extends ServiceImpl<UserMapper, User> implements QueryService {


    public List<User> queryUID(User user, HttpServletRequest request) throws IOException {

//        User user = new User();
        if (user.getId() != null) {
            user = getHex(user);
        }

        // 获取客户端IP
        user.setTableName(getUserTableName(user.getHex()));
//        System.out.println(user.getTableName());
//        // 获取核心配置文件
//        InputStream resourceAsStream = Resources.getResourceAsStream("src/main/resources/mapper/UserMapper.xml");
//        // 获取session工厂对象
//        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
//        // 获得session会话对象
//        SqlSession sqlSession = sqlSessionFactory.openSession(true);
//        // 执行操作 参数为namespace+id
//        List<User> userList = sqlSession.selectList("com.linhei.queryuserid.mapper.UserMapper.queryUID", user);
//        List<User> users = this.baseMapper.queryUID(user.getTableName(), user.getHex());
        //        userList.add(user);

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);
        recordLog("queryUID", ip, "user:" + user); // 先记录后查询 防止查询失败不记录

        List<User> userList = this.baseMapper.queryUID(user); // 为list第一个值设置访问者ip
        userList.get(0).setIp(ip);

        return userList;
    }

    @Override
    public List<User> queryUserList(String tableName, String start, String length, HttpServletRequest request) {
        if (null == start) { // 当未指定start时，将start默认指定为0
            start = "0";
        }


        if (null == length) { // 当length未指定时，将length默认指定为5000
            length = "5000";
        } else if (length.equals("max")) { // 当length指定为max时，将查询表下所有数据
            length = String.valueOf(getTableCount(tableName));
        }
//        System.out.println(length);

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);

        // 将查询信息记录
        recordLog("queryUserList", ip, "tableName:" + tableName + " start:" + start + " length:" + length); // 先记录后查询 防止查询失败不记录

        List<User> userList = this.baseMapper.queryUserList(tableName, start, length); // 为list第一个值设置访问者ip
        userList.get(0).setIp(ip);

        return userList;
    }

    @Override
    public String getUserTableName(String hex) {

        String tableName;
        try {
//            System.out.println(hex);
            tableName = "user_" + hex.substring(0, 2);
//            System.out.println(user.getHexTop());
        } catch (Exception e) {
            tableName = "user_" + hex.charAt(0);
//            System.out.println(user.getHexTop());
            e.printStackTrace();
            log(e);
            System.out.println("单字符");
        }
        return tableName;
    }

    @Override
    public boolean update(User user, HttpServletRequest request) {
        user = getHex(user);
//        System.out.println(user.toString());

        user.setIp(IpUtil.getIpAddr(request)); // 配置ip

        // 将查询信息记录
        recordLog("update", user.getIp(), user.toString()); // 先记录后查询 防止查询失败不记录
        return this.baseMapper.update(user);
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
        String getHex = String.format("%16x", crc32.getValue());    // 10进制转16进制
        user.setHex(getHex.trim()); // 去除空字符
        user.setTableName(getUserTableName(user.getHex()));
        user.setUpdateTime(new Date());
        return user;
    }


    /**
     * user1为返回的最终结果
     * user2为查询数据库的结果
     * user为用于通过id转crc32并转16进制的结果
     *
     * @param id      用户id
     * @param request 请求信息
     * @return user1
     */
    @Override
    public User getBiliUsername(long id, HttpServletRequest request) {

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);

        // 将查询信息记录
        recordLog("getBiliUsername", ip, String.valueOf(id));    // 先记录后查询 防止查询失败不记录

        BufferedReader bufIn = null;

        try {
            URL url = new URL("https://api.bilibili.com/x/web-interface/card?mid=" + id);
            URLConnection urlConn = url.openConnection();
            //设置请求属性，有部分网站不加这句话会抛出IOException: Server returned HTTP response code: 403 for URL异常
            //如：b站
            urlConn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");

            bufIn = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            log(e);
        }
        User user = new User(); // 需要查询的用户
        user.setId(id);

        User user1 = getHex(user);  // 获取hex值和当前时间
        List<User> list = new ArrayList<>(); // 查询数据库中符合条件的结果
        try {
            list = queryUID(user, request); // 对数据库进行查询 获取用户信息
        } catch (IOException e) {
            e.printStackTrace();
            log(e);
        }

//        User user = new User(); // 数据库中的用户
//        System.out.println(list.get(0));
        for (User value : list) {
//            System.out.println("user:" + value);
            if (value.getId() == id) {  // 判断用户是否为指定用户的ID 防止信息更改错误
                user = value;   // 将用户更新为查询的结果
                break;
            }
        }


        String line = "";
        StringBuilder textStr = new StringBuilder();
        while (line != null) {
            for (int i = 0; i < 10; i++)      //将10行内容追加到textStr字符串中
                try {
                    assert bufIn != null;
                    line = bufIn.readLine();

                    if (line != null)
                        textStr.append(line);
                } catch (IOException e) {
                    e.printStackTrace();
                    log(e);
                }
            try {
                //将img标签正则封装对象再调用matcher方法获取一个Matcher对象
                final Matcher textMatcher = Pattern.compile("\"mid\":\"(\\d+)\",\"name\":\"([\\W\\w]*)\",\"approve\"").matcher(textStr.toString());
                while (textMatcher.find())          //查找匹配文本
                {
//                    System.out.println(textMatcher.group());
                    user1.setId(Long.valueOf(textMatcher.group(1)));     //打印一遍
                    user1.setName(textMatcher.group(2));     //打印一遍
                }
            } catch (Exception e) {
                e.printStackTrace();
                log(e);
            }
        }

//        System.out.println(user2.toString());
//        System.out.println(user);
//        System.out.println(user1);
        if (user.getName() == null) { // 判断用户是否拥有用户名
            user1.setAlias(user.getAlias());
            update(user1, request);
        } else if (!user.getName().equals(user1.getName())) {  // 判断用户是否更改用户名
            user1.setAlias(user.getName());
            update(user1, request); // 并将更改上传到数据库
        } else if (user.getAlias() != null) {
            user1.setAlias(user.getAlias());
        }


        user1.setIp(ip); // 将ip赋予user1


        return user1;
    }

    @Override
    public Integer getTableCount(String tableName) {
        return this.baseMapper.getTableCount(tableName);
    }

    /**
     * @param methodName 方法名
     * @param ip         IP地址
     * @param content    查询的信息
     */
    public static void recordLog(String methodName, String ip, String content) {
        String information = methodName + "\tIP=" + ip + "\t查询信息=" + content + "\tTime=" + new Date();

        if (ip.equals("127.0.0.1")) {
            FileUtils.fileLinesWrite("opt//javaapps//log//LocalHostIP//" + methodName + ".txt",
                    information,
                    true);
        } else {
            FileUtils.fileLinesWrite("opt//javaapps//log//ClientIP//" + methodName + ".txt",
                    information,
                    true);
        }

    }

    /**
     * 将错误信息记录到日志文件中
     *
     * @param e Exception
     */
    public static void log(Exception e) {
        FileUtils.fileLinesWrite("opt//javaapps//log//log.txt", e.toString(), true);
    }
}
