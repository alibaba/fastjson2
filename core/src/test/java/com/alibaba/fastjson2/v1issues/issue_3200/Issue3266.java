package com.alibaba.fastjson2.v1issues.issue_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3266 {
    @Test
    public void test_for_issue() {
        VO vo = new VO();
        vo.type = Color.Black;

        assertEquals("1003",
                JSON.toJSONString(vo.type));

        String str = JSON.toJSONString(vo);
        assertEquals("{\"type\":1003}", str);

        VO vo2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.type, vo2.type);
    }

    @Test
    public void test_for_issue_method() {
        V1 vo = new V1();
        vo.type = Color.Black;

        assertEquals("1003",
                JSON.toJSONString(vo.type));

        String str = JSON.toJSONString(vo);
        assertEquals("{\"type\":1003}", str);

        V1 vo2 = JSON.parseObject(str, V1.class);
        assertEquals(vo.type, vo2.type);
    }

    public static class VO {
        public Color type;
    }

    public static class V1 {
        private Color type;

        public Color getType() {
            return type;
        }

        public void setType(Color type) {
            this.type = type;
        }
    }

    public enum Color {
        Red(1001),
        White(1002),
        Black(1003),
        Blue(1004);

        private final int code;

        private Color(int code) {
            this.code = code;
        }

        @JSONField
        public int getCode() {
            return code;
        }

        @JSONCreator
        public static Color from(int code) {
            for (Color v : values()) {
                if (v.code == code) {
                    return v;
                }
            }

            throw new IllegalArgumentException("code " + code);
        }
    }
}
