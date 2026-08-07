package com.alibaba.fastjson2.benchmark.along.vo;

public class HarmDTO {
    private Long targetId;
    private int type;

    private float value;
    private boolean dead;
    private long real;
    private float maxHp;
    private float curHp;

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public long getReal() {
        return real;
    }

    public void setReal(long real) {
        this.real = real;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(float maxHp) {
        this.maxHp = maxHp;
    }

    public float getCurHp() {
        return curHp;
    }

    public void setCurHp(float curHp) {
        this.curHp = curHp;
    }
}
