package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3266_mixedin {
    @Test
    public void test_for_issue() throws Exception {
        com.alibaba.fastjson2.JSON.mixIn(Color.class, ColorMixedIn.class);

        VO vo = new VO();
        vo.type = Color.Black;

        assertEquals("1003",
                JSON.toJSONString(vo.type));

        String str = JSON.toJSONString(vo);
        assertEquals("{\"type\":1003}", str);

        VO vo2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.type, vo2.type);
    }

    public static class VO {
        public Color type;
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

        public int getCode() {
            return code;
        }

        public static Color from(int code) {
            for (Color v : values()) {
                if (v.code == code) {
                    return v;
                }
            }

            throw new IllegalArgumentException("code " + code);
        }
    }

    public static class ColorMixedIn {
        @JSONField
        public int getCode() {
            return 0;
        }

        @JSONCreator
        public static Color from(int code) {
            return null;
        }
    }
}
