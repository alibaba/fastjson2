package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2443 {
    @Test
    public void test() {
        User user = new User();
        assertEquals(
                "{\"登录名\":\"\",\"用户名\":\"\",\"年龄\":\"\",\"性别\":\"\",\"出生日期\":\"\"}",
                JSON.toJSONString(user));
    }

    @JSONType(orders = {"登录名", "用户名", "年龄", "性别", "出生日期"})
    @Getter
    @Setter
    static class User {
        @JSONField(name = "登录名")
        String loginName = "";
        @JSONField(name = "用户名")
        String userName = "";
        @JSONField(name = "年龄")
        String userAge = "";
        @JSONField(name = "性别")
        String userSex = "";
        @JSONField(name = "出生日期")
        String userBithday = "";
    }

    @Test
    public void test1() {
        User1 user = new User1();
        assertEquals(
                "{\"登录名\":\"\",\"用户名\":\"\",\"年龄\":\"\",\"性别\":\"\",\"出生日期\":\"\"}",
                JSON.toJSONString(user));
    }

    @Getter
    @Setter
    static class User1 {
        @JSONField(name = "登录名", ordinal = 1)
        String loginName = "";
        @JSONField(name = "用户名", ordinal = 2)
        String userName = "";
        @JSONField(name = "年龄", ordinal = 3)
        String userAge = "";
        @JSONField(name = "性别", ordinal = 4)
        String userSex = "";
        @JSONField(name = "出生日期", ordinal = 5)
        String userBithday = "";
    }
}
