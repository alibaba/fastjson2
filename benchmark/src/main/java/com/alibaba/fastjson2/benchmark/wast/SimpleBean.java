package com.alibaba.fastjson2.benchmark.wast;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SimpleBean {
    private int id;
    private long version;
    private double percent;
    private String name;

    @com.alibaba.fastjson.annotation.JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @com.alibaba.fastjson2.annotation.JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date date;
    private SimpleEnum simpleEnum;
    private Map<String, Object> mapInstance;
    private List<Object> list;

    private List<Object> versions;

    public enum SimpleEnum {
        EnumOne,
        EnumTwo,
        EnumThree
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SimpleEnum getSimpleEnum() {
        return simpleEnum;
    }

    public void setSimpleEnum(SimpleEnum simpleEnum) {
        this.simpleEnum = simpleEnum;
    }

    public Map<String, Object> getMapInstance() {
        return mapInstance;
    }

    public void setMapInstance(Map<String, Object> mapInstance) {
        this.mapInstance = mapInstance;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }

    public List<Object> getVersions() {
        return versions;
    }

    public void setVersions(List<Object> versions) {
        this.versions = versions;
    }
}
