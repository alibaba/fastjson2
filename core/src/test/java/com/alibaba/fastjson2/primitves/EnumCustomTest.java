package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumCustomTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.size = Size.Large;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"size\":101}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.size, bean1.size);

        Bean bean2 = JSON.parseObject(str).to(Bean.class);
        assertEquals(bean.size, bean2.size);
    }

    public static class Bean {
        public Size size;
    }

    public interface XEnum {
        @JSONField(value = true)
        int getValue();
    }

    public enum Size implements XEnum {
        Small(99),
        Medium(100),
        Large(101),
        XLarge(102);

        private final int value;

        Size(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.size = Size1.Large;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"size\":101}", str);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.size, bean1.size);

        Bean1 bean2 = JSON.parseObject(str).to(Bean1.class);
        assertEquals(bean.size, bean2.size);
    }

    public static class Bean1 {
        public Size1 size;
    }

    public enum Size1 {
        Small(99),
        Medium(100),
        Large(101),
        XLarge(102);

        @JSONField(value = true)
        public final int value;

        Size1(int value) {
            this.value = value;
        }
    }
}
