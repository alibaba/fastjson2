package com.alibaba.fastjson2.v1issues.issue_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3266_str {
    @Test
    public void test_for_issue() {
        VO vo = new VO();
        vo.type = Color.Black;

        assertEquals("\"黑色\"",
                JSON.toJSONString(vo.type));

        String str = JSON.toJSONString(vo);
        assertEquals("{\"type\":\"黑色\"}", str);

        VO vo2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.type, vo2.type);
    }

    public static class VO {
        public Color type;
    }

    public enum Color {
        Red(1001, "红色"),
        White(1002, "白色"),
        Black(1003, "黑色"),
        Blue(1004, "蓝色");

        private final int code;
        private final String name;

        private Color(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        @JSONField
        public String getName() {
            return name;
        }

        @JSONCreator
        public static Color from(String name) {
            for (Color v : values()) {
                if (v.name.equals(name)) {
                    return v;
                }
            }

            throw new IllegalArgumentException("name " + name);
        }
    }
}
