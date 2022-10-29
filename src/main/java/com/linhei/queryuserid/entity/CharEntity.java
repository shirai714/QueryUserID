package com.linhei.queryuserid.entity;

import java.util.Date;

/**
 * @author linhei
 */
public class CharEntity {

    /**
     * id:       用于前端排序
     * hex:      用户
     * time:     弹幕发送时间
     * bChar:    弹幕内容
     * timeline: 弹幕在视频中的时间
     */
    int id;
    String hex;
    Date time;
    String bChar;
    String timeline;
    Long uid;
    String username;


    /**
     * 无参构造
     */
    public CharEntity() {
    }

    /**
     * 有参构造
     *
     * @param id:       用于前端排序
     * @param hex:      用户
     * @param time:     弹幕发送时间
     * @param bChar:    弹幕内容
     * @param timeline: 弹幕在视频中的时间
     */
    public CharEntity(int id, String hex, Date time, String bChar, String timeline) {
        this.id = id;
        this.hex = hex;
        this.time = time;
        this.bChar = bChar;
        this.timeline = timeline;
    }

    @Override
    public String toString() {
        return "CharEntity{" +
                "id=" + id +
                ", hex='" + hex + '\'' +
                ", time=" + time +
                ", bChar='" + bChar + '\'' +
                ", timeline='" + timeline + '\'' +
                ", uid=" + uid +
                ", username='" + username + '\'' +
                '}';
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getbChar() {
        return bChar;
    }

    public void setbChar(String bChar) {
        this.bChar = bChar;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
