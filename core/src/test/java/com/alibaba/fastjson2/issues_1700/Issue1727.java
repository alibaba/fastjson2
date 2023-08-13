package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1727 {
    @Test
    public void test() {
        final Bean bean = new Bean();
        bean.color = Color.BLUE;

        assertEquals("{\"color\":5}", JSON.toJSONString(bean));
    }

    public enum Color {
        RED(1),
        YELLOW(3),
        BLUE(5);

        final Integer value;

        Color(Integer value) {
            this.value = value;
        }
    }

    @Getter
    public static class Bean {
        public Color color;

        public Integer getColor() {
            return color.value;
        }
    }

    @Test
    public void test1() {
        final Bean1 bean = new Bean1();
        bean.color = Color.BLUE;

        assertEquals("{\"color\":5}", JSON.toJSONString(bean));
    }

    @Getter
    public static class Bean1 {
        public Color color;

        public int getColor() {
            return color.value;
        }
    }
}
