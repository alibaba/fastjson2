package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kraity
 */
public class Issue1578 {
    @Getter
    @AllArgsConstructor
    public enum GenderEnum {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");

        @JSONField(value = true)
        private final Integer code;
        private final String name;
    }

    @Getter
    @Setter
    @ToString
    public static class User {
        private String name;
        private GenderEnum gender;
    }

    @Test
    public void test() {
        User user0 = JSON.parseObject("{\"name\":\"张三丰\",\"gender\":0}", User.class);
        assertNotNull(user0);
        assertEquals("张三丰", user0.getName());
        assertSame(GenderEnum.UNKNOWN, user0.getGender());

        User user1 = JSON.parseObject("{\"name\":\"张三丰\",\"gender\":1}", User.class);
        assertNotNull(user1);
        assertEquals("张三丰", user1.getName());
        assertSame(GenderEnum.MALE, user1.getGender());

        User user2 = JSON.parseObject("{\"name\":\"张三丰\",\"gender\":\"2\"}", User.class);
        assertNotNull(user2);
        assertEquals("张三丰", user2.getName());
        assertSame(GenderEnum.FEMALE, user2.getGender());

        User user3 = JSON.parseObject("{\"name\":\"张三丰\",\"gender\":\"0\"}", User.class);
        assertNotNull(user3);
        assertEquals("张三丰", user3.getName());
        assertSame(GenderEnum.UNKNOWN, user3.getGender());
    }

    @Getter
    @AllArgsConstructor
    public enum GenderEnum2 {
        MALE(-6, "男"),
        FEMALE(-7, "女");
        private final Integer code;
        private final String name;
    }

    @Getter
    @Setter
    @ToString
    public static class User2 {
        private String name;
        private GenderEnum2 gender;
    }

    @Test
    public void test_2x4() {
        User2 user2 = JSON.parseObject("{\"gender\":\"0\",\"name\":\"张三丰\"}", User2.class);
        assertNotNull(user2);
        assertEquals("张三丰", user2.getName());
        assertSame(GenderEnum2.MALE, user2.getGender());

        User2 user3 = JSON.parseObject("{\"gender\":\"1\",\"name\":\"张三丰\"}", User2.class);
        assertNotNull(user3);
        assertEquals("张三丰", user3.getName());
        assertSame(GenderEnum2.FEMALE, user3.getGender());
    }
}
