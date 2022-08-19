package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumValueMixinTest {
    @Test
    public void test() {
        JSON.mixIn(Size.class, SizeMixin.class);

        Bean bean = new Bean();
        bean.size = Size.Large;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"size\":101}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.size, bean1.size);
    }

    public interface SizeMixin {
        @JSONField(value = true)
        int getValue();
    }

    public static class Bean {
        public Size size;
    }

    public enum Size {
        Small(99),
        Medium(100),
        Large(101),
        XLarge(102);

        private final int value;

        Size(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
