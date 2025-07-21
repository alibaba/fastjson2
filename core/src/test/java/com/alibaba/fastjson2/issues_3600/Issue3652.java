package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3652 {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ValueEnumJson {
    }

    public interface ValueEnum {
        /**
         * 枚举值.
         */
        @JSONField
        int value();
    }

    public enum OrderType implements ValueEnum {
        BIG("大", 10),
        LITTLE("小", 20),
        OTHER("其他", 102);

        private final String bizName;
        private final int value;

        OrderType(String name, int value) {
            this.bizName = name;
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }

        public String bizName() {
            return bizName;
        }
    }

    @Test
    public void test() {
        for (OrderType e : OrderType.values()) {
            String str = JSON.toJSONString(e);
            assertEquals(Integer.toString(e.value()), str);

            OrderType e1 = JSON.parseObject(str, OrderType.class);
            assertEquals(e, e1);
        }
    }
}
