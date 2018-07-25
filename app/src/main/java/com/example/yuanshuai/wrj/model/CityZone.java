package com.example.yuanshuai.wrj.model;

import java.sql.Timestamp;


public class CityZone
{
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJianPin() {
        return jianPin;
    }

    public void setJianPin(String jianPin) {
        this.jianPin = jianPin;
    }

    public String getQuanPin() {
        return quanPin;
    }

    public void setQuanPin(String quanPin) {
        this.quanPin = quanPin;
    }

    public int getPopularLevel() {
        return popularLevel;
    }

    public void setPopularLevel(int popularLevel) {
        this.popularLevel = popularLevel;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    private String name;

    private int type;

    private String code;

    private String jianPin;

    private String quanPin;

    private int popularLevel;

    private Timestamp createTime;

    private boolean deleted = false;

    private int parentId;
}
