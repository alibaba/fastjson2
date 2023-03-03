package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OverrideTest {
    @Test
    public void test_nullAsEmptyStr() {
        B b = new B();

        String json = JSON.toJSONString(b, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":\"\"}", json);
    }

    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;
        assertEquals("{}", JSON.toJSONString(bean));

        Bean bean1 = JSON.parseObject("{\"id\":123}", Bean.class);
        assertEquals(0, bean1.id);
    }

    @Test
    public void test1() throws Exception {
        Bean1 bean = new Bean1();
        bean.id = 1001;
        assertEquals("{}", JSON.toJSONString(bean));

        Bean1 bean1 = JSON.parseObject("{\"id\":123}", Bean1.class);
        assertEquals(0, bean1.id);
    }

    public abstract static class A {
        public abstract Object getValue();
    }

    public static class B
            extends A {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public interface IBean {
        @JSONField(serialize = false)
        int getId();

        @JSONField(deserialize = false)
        void setId(int id);
    }

    public static class Bean
            implements IBean {
        private int id;
        @Override
        public int getId() {
            return id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }
    }

    public abstract static class Bean1Abastract {
        @JSONField(serialize = false)
        public abstract int getId();

        @JSONField(deserialize = false)
        public abstract void setId(int id);
    }

    public static class Bean1
            extends Bean1Abastract {
        private int id;

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }
    }
}
