package com.alibaba.fastjson2.benchmark.along.vo;

public enum SkillCategory {
    ATTACKED_PASSIVE(1, " 受创时被动触发技能"),
    ACTIVE(2, "主动"),
    ATTACK_PASSIVE(3, "攻击时被动触发技能"),
    ATTRIBUTE_ATTRIBUTE(4, "属性技能");

    private final int id;
    private final String message;

    SkillCategory(int id, String message) {
        this.id = id;

        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
