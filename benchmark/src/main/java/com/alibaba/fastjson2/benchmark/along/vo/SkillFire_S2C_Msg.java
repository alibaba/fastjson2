package com.alibaba.fastjson2.benchmark.along.vo;

import java.util.ArrayList;
import java.util.List;

public class SkillFire_S2C_Msg {
    private Long attackerId;
    private SkillCategory skillCategory;
    private int index;
    private List<HarmDTO> harmList = new ArrayList<>();
    private List<Long> param1 = new ArrayList<>();

    public Long getAttackerId() {
        return attackerId;
    }

    public void setAttackerId(Long attackerId) {
        this.attackerId = attackerId;
    }

    public SkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<HarmDTO> getHarmList() {
        return harmList;
    }

    public void setHarmList(List<HarmDTO> harmList) {
        this.harmList = harmList;
    }

    public List<Long> getParam1() {
        return param1;
    }

    public void setParam1(List<Long> param1) {
        this.param1 = param1;
    }
}
