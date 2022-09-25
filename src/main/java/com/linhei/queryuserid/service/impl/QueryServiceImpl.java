package com.linhei.queryuserid.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linhei.queryuserid.entity.User;
import com.linhei.queryuserid.mapper.UserMapper;
import com.linhei.queryuserid.service.QueryService;
import com.linhei.queryuserid.utils.IpUtil;
import com.linhei.queryuserid.utils.RedisUtil;
import com.linhei.queryuserid.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;


/**
 * @author linhei
 */


@EnableScheduling // 启动定时任务注解
@Service
@Slf4j
public class QueryServiceImpl extends ServiceImpl<UserMapper, User> implements QueryService {

    @Autowired
    RedisUtil redisUtil;

    Util util = new Util();

    @Override
    public List<User> queryUiD(User user, HttpServletRequest request) {

        if (user.getId() != null) {
            user = util.getHex(user);
        }

        // 获取客户端IP
        user.setTableName(util.getUserTableName(user.getHex()));

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);
        // 先记录后查询 防止查询失败不记录
        util.recordLog("queryUID", ip, "user:" + user);

        List<User> userList = this.baseMapper.queryUiD(user);

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

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);

        // 先记录后查询 防止查询失败不记录
        util.recordLog("queryUserList", ip, "tableName:" + tableName + " start:" + start + " length:" + length);

        List<User> userList = this.baseMapper.queryUserList(tableName, start, length);

        // 若userList大小为0则返回null
        if (userList.size() == 0) {
            return null;
        }
        // 为list第一个值设置访问者ip
        userList.get(0).setIp(ip);

        return userList;
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
            user = util.getHex(user);
        }

        // 配置ip
        user.setIp(IpUtil.getIpAddr(request));

        // 先记录后查询 防止查询失败不记录
        util.recordLog("update", user.getIp(), user.toString());

        return this.baseMapper.update(user);
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
    public User getUser(long id, HttpServletRequest request) {

        // 获取客户端IP地址
        String ip = IpUtil.getIpAddr(request);
        // 先记录后查询 防止查询失败不记录
        util.recordLog("getUser", ip, String.valueOf(id));
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
        User user1 = util.getHex(new User(id));

        final String reg = "\"mid\":\"\\d{1,11}\",\"name\":\"([\\W\\w]{1,16})\",\"approve\"";

        // 调用爬取API的方法
        user1 = util.getBiliApi(user1, id, reg, "https://api.bilibili.com/x/web-interface/card?mid=");

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
                user.setTableName(util.getUserTableName(user.getHex()));
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

    /**
     * @param user 实体类
     * @return 删除结果
     */
    @Override
    public Boolean deleteUser(User user) {
        return this.baseMapper.deleteUser(user);
    }

    /**
     * @return 表名列表
     */
    @Override
    public List<String> getTableList() {
        return this.baseMapper.getTableList();
    }


}
