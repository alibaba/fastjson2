package com.alibaba.fastjson3.benchmark.bean;

import java.util.List;
import java.util.Map;

/**
 * Simple POJO with multiple field types for benchmark testing.
 */
public class SimpleBean {
    private int id;
    private String name;
    private long version;
    private double percent;
    private boolean active;
    private List<Integer> scores;
    private Map<String, Object> extra;

    public SimpleBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
