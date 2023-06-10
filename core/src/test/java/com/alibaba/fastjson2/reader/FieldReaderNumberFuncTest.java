package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldReaderNumberFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.number = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"number\":123}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.number, bean1.number);

        Bean bean2 = JSON.parseObject(str).toJavaObject(Bean.class);
        assertEquals(bean.number, bean2.number);
    }

    private static class Bean {
        private Number number;

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1(123);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"number\":123}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.number, bean1.number);

        Bean1 bean2 = JSON.parseObject(str).toJavaObject(Bean1.class);
        assertEquals(bean.number, bean2.number);
    }

    private static class Bean1 {
        private final Number number;

        public Bean1(@JSONField(name = "number") Number number) {
            this.number = number;
        }

        public Number getNumber() {
            return number;
        }
    }
}
