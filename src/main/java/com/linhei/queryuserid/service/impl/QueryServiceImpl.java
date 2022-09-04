package com.linhei.queryuserid.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.mapper.UserMapper;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.FileUtilss;
import com.linhei.queryuserid.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * @author linhei
 */

/*
 启动定时任务注解
 */
@EnableScheduling
@Service
@Slf4j
public class QueryServiceImpl extends ServiceImpl<UserMapper, User> implements QueryService {

    @Override
    public List<User> queryUiD(User user, HttpServletRequest request) {

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
        // 先记录后查询 防止查询失败不记录
        recordLog("queryUID", ip, "user:" + user);


        List<User> userList = this.baseMapper.queryUID(user);

        // 若userList为null则返回null
        if (userList == null || userList.size() == 0) {
            return null;
        }

        // 判断传入参数中是否有id
        if (user.getId() != null) {
            // 使用迭代器遍历
            Iterator<User> iterator = userList.iterator();
            while (iterator.hasNext()) {
                // 若匹配则跳出本次循环
                if (iterator.next().getId().equals(user.getId())) {
                    continue;
                }
                // 不匹配则删除
                iterator.remove();
            }
        }

        // 若userList为null则返回null
        if (userList.size() == 0) {
            return null;
        }

        // 为list第一个值设置访问者ip
        userList.get(0).setIp(ip);
        return userList;
    }


    @Override
    public List<User> queryUserList(String tableName, String start, String length, HttpServletRequest request) {
        // 当未指定start时，将start默认指定为0
        if (null == start) {
            start = "0";
        }


        // 当length未指定时，将length默认指定为5000
        if (null == length) {
            length = "5000";
        } else if ("max".equals(length)) {
            // 当length指定为max时，将查询表下所有数据
            length = String.valueOf(getTableCount(tableName));
        }
//        System.out.println(length);

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);

        // 先记录后查询 防止查询失败不记录
        recordLog("queryUserList", ip, "tableName:" + tableName + " start:" + start + " length:" + length);


        List<User> userList = this.baseMapper.queryUserList(tableName, start, length);

        // 若userList大小为0则返回null
        if (userList.size() == 0) {
            return null;
        }
        // 为list第一个值设置访问者ip
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
            log("getUserTableName\t hex=" + hex, e);

            System.out.println("单字符");
        }
        return tableName;
    }


    /**
     * update()
     *
     * @param user    用户对象
     * @param request 请求
     * @return 修改结果
     */
    @Override
    public boolean update(User user, HttpServletRequest request) {
        // 当用户id为空时不允许修改
        if (user.getId() == null) {
            return false;
        } else if (user.getTableName() == null || user.getHex() == null || "".equals(user.getTableName()) || "".equals(user.getHex())) {
            // 只有当表名和hex为空时才调用获取hex的方法
            user = getHex(user);
        }

        // 配置ip
        user.setIp(IpUtil.getIpAddr(request));

        // 先记录后查询 防止查询失败不记录
        recordLog("update", user.getIp(), user.toString());

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
        // 10进制转16进制
        String getHex = String.format("%16x", crc32.getValue());
        // 去除空字符
        user.setHex(getHex.trim());
        user.setTableName(getUserTableName(user.getHex()));
        user.setUpdateTime(new Date());
        return user;
    }


    /**
     * user1为返回的最终结果
     * user为用于通过id转crc32并转16进制的结果和在数据库中查询的结果
     *
     * @param id      用户id
     * @param request 请求信息
     * @return user1
     */
    @Override
    public User getBiliUsername(long id, HttpServletRequest request) {

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);
        // 先记录后查询 防止查询失败不记录
        recordLog("getBiliUsername", ip, String.valueOf(id));
        // 需要查询的用户 用于比对用户名是否更新
        User user = new User(id);
        // 查询数据库中符合条件的结果
        List<User> list;
        // 对数据库进行查询 获取用户信息
        list = queryUiD(user, request);
        // 若list大小为0则返回null
        if (list == null || list.size() == 0) {
            return null;
        }

        for (User value : list) {
            // 判断用户是否为指定用户的ID 防止信息更改错误
            if (value.getId() == id) {
                // 将用户更新为查询的结果
                user = value;
                break;
            }
        }

        // 获取hex值和当前时间
        User user1 = getHex(new User(id));

        final String reg = "\"mid\":\"\\d{1,11}\",\"name\":\"([\\W\\w]{1,16})\",\"approve\"";

        // 调用爬取API的方法
        user1 = getBiliApi(user1, id, reg, "https://api.bilibili.com/x/web-interface/card?mid=");

        if (null == user1) {
            return null;
        }

        // 用于判断用户是否拥有用户名
        if (user.getName() == null) {
            user1.setAlias(user.getAlias());
            update(user1, request);
        } else if (!user.getName().equals(user1.getName())) {
            // 判断用户是否更改用户名
            user1.setAlias(user.getName());
            // 并将更改上传到数据库
            update(user1, request);
        } else if (user.getAlias() != null) {
            // 更新曾用名
            user1.setAlias(user.getAlias());
        }

        // 将查询者ip赋予user1
        user1.setIp(ip);

        return user1;
    }


    /**
     * 获取表内的数据量
     *
     * @param tableName 表名
     * @return 表的数据量
     */
    @Override
    public Integer getTableCount(String tableName) {
        return this.baseMapper.getTableCount(tableName);
    }


    /**
     * 将数据插入表中
     *
     * @param user 实体类
     * @return 插入结果
     */
    @Override
    public boolean insertUser(User user) {
        // 判断传入实体类的ID和Hex是否为空
        if (user.getHex() != null && user.getId() != null) {
            // 若表名未传入则调用方法获取表名
            if (user.getTableName() == null) {
                user.setTableName(getUserTableName(user.getHex()));
            }
            //将当前系统时间加入用户实体类中            user.setUpdateTime(new Date()); // 由于MySQL时间字段增加了时间随更改而更改故而不需要程序中添加时间
            return this.baseMapper.insertUser(user);
        }
        return false;
    }


    /**
     * @param tableName 表名
     * @return 创建结果
     */
    @Override
    public Integer createTable(String tableName) {

        return this.baseMapper.createTable(tableName);
    }

    @Override
    public Boolean deleteUser(User user) {
        return this.baseMapper.deleteUser(user);
    }

    @Override
    public List<String> getTableList() {
        return this.baseMapper.getTableList();
    }


    // 表名    private List<String> tableList;

    /**
     * 定时获取数据库中所有的表名
     */
//    @PostConstruct // 项目启动后执行注解    @Scheduled(cron = "0 0 */8 * * ?") // 设置定时任务注解 每过8小时执行一次
//    private void getTableName() {
//        tableList = getTableList();
//    }


    /**
     * @param methodName 方法名
     * @param ip         访问者IP地址
     * @param content    查询的信息
     */
    public static void recordLog(String methodName, String ip, String content) {
        String information = methodName + "\tIP=" + ip + "\t查询信息=" + content + "\tTime=" + new Date();

        if ("127.0.0.1".equals(ip)) {
            FileUtilss.fileLinesWrite("opt//javaApps//log//LocalHostIP//" + methodName + ".txt",
                    information,
                    true);
        } else {
            FileUtilss.fileLinesWrite("opt//javaApps//log//ClientIP//" + methodName + ".txt",
                    information,
                    true);
        }

    }

    /**
     * 将错误信息记录到日志文件中
     *
     * @param e Exception
     */
    public static void log(String methodName, Exception e) {
        FileUtilss.fileLinesWrite("opt//javaApps//log//log.txt", methodName + "\t" + e.toString(), true);
    }

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
            log("getBiliUsername\tURLConnection", e);
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
                    e.printStackTrace();
                    log("getBiliUsername\t空指针异常", e);
                    log.warn("getBiliUsername\t空指针异常" + e.getMessage());
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
                    FileUtilss.fileLinesWrite("opt//javaApps//log//deleteLog.txt",
                            "User:\t" + user + "\t删除结果：" + deleteUser(user), true);
                    return null;
                }
            }

        }

        return user;
    }

    /**
     * 签到用定时执行脚本
     *
     * @param url           链接
     * @param cookie        cookie
     * @param authorization 认证
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
                urlConn.addRequestProperty("authorization", authorization);
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
