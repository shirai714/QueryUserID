//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.linhei.queryuserid.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linhei.queryuserid.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface QueryService extends IService<User> {
    List<User> queryUID(User user, HttpServletRequest request) throws IOException;

    List<User> queryUserList(String tableName, String start, String length, HttpServletRequest request) throws IOException;

    String getUserTableName(String hex);

    boolean update(User user, HttpServletRequest request);

    User getBiliUsername(long id, HttpServletRequest request) throws MalformedURLException;

    Integer getTableCount(String tableName);

}
