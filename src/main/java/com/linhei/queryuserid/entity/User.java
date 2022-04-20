package com.linhei.queryuserid.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 用户实体类
 */
//@TableName("user_1a")
public class User {

    /**
     * id:用户id
     * hex:用户id处理后的十六进制值
     * name:用户名
     * alias:曾用名
     * updateTime:修改时间
     * hexTop2:用户id处理后的十六进制值的前2位
     * hexTop1:用户id处理后的十六进制值的前1位
     */

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("user_key")
    private String hex;
    @TableField("user_name")
    private String name;
    @TableField("user_alias")
    private String alias;
    //    @TableField("table_name")

    @TableField("update_time")
    private Date updateTime;

    private String tableName;
    private Long selectTime;
    private String ip;

    //    Getter and Setter方法


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getSelectTime() {
        return selectTime;
    }

    public void setSelectTime(Long selectTime) {
        this.selectTime = selectTime;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    //    无参构造
    public User() {
    }

    //    有参构造

    public User(Long id, String hex, String name, String alias, String hexTop2, Date updateTime, Long selectTime, String ip) {
        this.id = id;
        this.hex = hex;
        this.name = name;
        this.alias = alias;
        this.tableName = hexTop2;
        this.updateTime = updateTime;
        this.selectTime = selectTime;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", hex='" + hex + '\'' +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", updateTime=" + updateTime +
                ", tableName='" + tableName + '\'' +
                ", selectTime=" + selectTime +
                ", ip='" + ip + '\'' +
                '}';
    }
}
