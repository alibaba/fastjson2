package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue752 {
    @Test
    public void test() {
        Bean bean = new Bean(new Class<?>[]{int.class, int.class});
        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName);
        JSONB.dump(bytes);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.issues.Issue752$Bean\",\n" +
                "\t\"param\":[\n" +
                "\t\t\"int\",\n" +
                "\t\t\"int\"\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(bytes));

        Bean bean1 = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportClassForName);
        assertArrayEquals(bean.param, bean1.param);
    }

    public static class Bean {
        private Class<?>[] param;

        public Bean(@JSONField(name = "param") Class<?>[] param) {
            this.param = param;
        }

        public Class<?>[] getParam() {
            return param;
        }
    }
}
