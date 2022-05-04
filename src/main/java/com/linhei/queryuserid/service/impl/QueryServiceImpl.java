package com.linhei.queryuserid.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.mapper.UserMapper;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.FileUtils;
import com.linhei.queryuserid.utils.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class QueryServiceImpl extends ServiceImpl<UserMapper, User> implements QueryService {

    private final static Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);

    public List<User> queryUID(User user, HttpServletRequest request) {

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

        // 若userList大小为0则返回null
        if (userList.size() == 0) {
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

        // 为list第一个值设置访问者ip
        userList.get(0).setIp(ip);
        return userList;
    }


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
     * user2为查询数据库的结果
     * user为用于通过id转crc32并转16进制的结果
     *
     * @param id      用户id
     * @param request 请求信息
     * @return user1
     */
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
            log("getBiliUsername\tURLConnection", e);
        }
        // 需要查询的用户
        User user = new User();
        user.setId(id);

        // 获取hex值和当前时间
        User user1 = getHex(user);
        // 查询数据库中符合条件的结果
        List<User> list;

        // 对数据库进行查询 获取用户信息
        list = queryUID(user, request);

        // 若list大小为0则返回null
        if (list.size() == 0) {
            return null;
        }


//        User user = new User(); // 数据库中的用户
//        System.out.println(list.get(0));
        for (User value : list) {
//            System.out.println("user:" + value);
            // 判断用户是否为指定用户的ID 防止信息更改错误
            if (value.getId() == id) {
                // 将用户更新为查询的结果
                user = value;
                break;
            }
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
                    log("getBiliUsername\t防止空指针异常", e);
                }
            }
            String strPattern = "\"mid\":\"(\\d+)\",\"name\":\"([\\W\\w]*)\",\"approve\"";
            //将img标签正则封装对象再调用matcher方法获取一个Matcher对象
            final Matcher textMatcher = Pattern.compile(strPattern).matcher(textStr.toString());
            //查找匹配文本
            while (textMatcher.find()) {
//                    System.out.println(textMatcher.group());
                // 将匹配结果添加到user1中
                user1.setId(Long.valueOf(textMatcher.group(1)));
                user1.setName(textMatcher.group(2));
            }

        }

//        System.out.println(user2.toString());
//        System.out.println(user);
//        System.out.println(user1);
        // 判断用户是否拥有用户名
        if (user.getName() == null) {
            user1.setAlias(user.getAlias());
            update(user1, request);
        } else if (!user.getName().equals(user1.getName())) {
            // 判断用户是否更改用户名
            user1.setAlias(user.getName());
            // 并将更改上传到数据库
            update(user1, request);
        } else if (user.getAlias() != null) {
            user1.setAlias(user.getAlias());
        }

        // 将ip赋予user1
        user1.setIp(ip);


        return user1;
    }


    /**
     * 获取表内的数据量
     *
     * @param tableName 表名
     * @return 表的数据量
     */
    public Integer getTableCount(String tableName) {
        return this.baseMapper.getTableCount(tableName);
    }


    /**
     * 将数据插入表中
     *
     * @param user 实体类
     * @return 插入结果
     */
    public boolean insertUser(User user) {
        // 判断传入实体类的ID和Hex是否为空
        if (user.getHex() != null && user.getId() != null) {
            // 若表名未传入则调用方法获取表名
            if (user.getTableName() == null) {
                user.setTableName(getUserTableName(user.getHex()));
            }
            //将当前系统时间加入用户实体类中
            user.setUpdateTime(new Date());
            return this.baseMapper.insertUser(user);
        }


        return false;
    }


    /**
     * @param tableName 表名
     * @return 创建结果
     */
    public Integer createTable(String tableName) {

        return this.baseMapper.createTable(tableName);
    }


    /**
     * @param methodName 方法名
     * @param ip         访问者IP地址
     * @param content    查询的信息
     */
    public static void recordLog(String methodName, String ip, String content) {
        String information = methodName + "\tIP=" + ip + "\t查询信息=" + content + "\tTime=" + new Date();

        if ("127.0.0.1".equals(ip)) {
            FileUtils.fileLinesWrite("opt//javaApps//log//LocalHostIP//" + methodName + ".txt",
                    information,
                    true);
        } else {
            FileUtils.fileLinesWrite("opt//javaApps//log//ClientIP//" + methodName + ".txt",
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
        FileUtils.fileLinesWrite("opt//javaApps//log//log.txt", methodName + "\t" + e.toString(), true);
    }
}
