package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1435 {
    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.type = Type.M;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"type\":\"102\"}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.type, bean1.type);
    }

    public interface BaseEnum {
        /**
         * 获取code
         *
         * @return {@link String}
         */
        String getCode();
    }

    public enum Type
            implements BaseEnum{
        X(101, "Big"),
        M(102, "Medium"),
        S(103, "Small");

        private final int code;
        private final String name;

        Type(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @JSONField(value = true)
        public String getCode() {
            return String.valueOf(code);
        }

        public String getName() {
            return name;
        }
    }

    public class Bean1 {
        public Type type;
    }
}
