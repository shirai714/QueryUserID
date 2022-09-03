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
    private String ip;

    //    Getter and Setter方法


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    /**
     * 无参构造
     */
    public User() {
    }

    /**
     * 有参构造
     *
     * @param id         id
     * @param hex        hex
     * @param name       姓名
     * @param alias      曾用名
     * @param hexTop2    前两位hex
     * @param updateTime 更新时间
     * @param ip         用户id
     */
    public User(Long id, String hex, String name, String alias, String hexTop2, Date updateTime, String ip) {
        this.id = id;
        this.hex = hex;
        this.name = name;
        this.alias = alias;
        this.tableName = hexTop2;
        this.updateTime = updateTime;
        this.ip = ip;
    }

    /**
     * 仅id构造方法
     *
     * @param id ID
     */
    public User(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder user = new StringBuilder();
        user.append("User{");
        if (id != null) {
            user.append("id='").append(id);
        }
        if (hex != null) {
            user.append("', hex='").append(hex);
        }
        if (name != null) {
            user.append("', name='").append(name);
        }
        if (alias != null) {
            user.append("', alias='").append(alias);
        }
        if (updateTime != null) {
            user.append("', updateTime='").append(updateTime);
        }
        if (tableName != null) {
            user.append("', tableName='").append(tableName);
        }
        if (ip != null) {
            user.append("', ip='").append(ip);
        }
        user.append("'}");
        return user.toString();

    }
}
