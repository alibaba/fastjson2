package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2448 {
    @Test
    public void test() {
        boolean defaultWriterAlphabetic = JSONFactory.isDefaultWriterAlphabetic();
        try {
            JSONFactory.setDefaultWriterAlphabetic(true);
            Bean bean = new Bean();
            bean.a1 = 1;
            bean.a2 = 2;
            bean.a3 = 3;
            assertEquals("{\"a2\":2,\"a1\":1,\"a3\":3}", JSON.toJSONString(bean));
        } finally {
            JSONFactory.setDefaultWriterAlphabetic(defaultWriterAlphabetic);
        }
    }

    @JSONType(alphabetic = false)
    public static class Bean {
        public int a2;
        public int a1;
        public int a3;
    }

    @Test
    public void test2() {
        boolean defaultWriterAlphabetic = JSONFactory.isDefaultWriterAlphabetic();
        try {
            JSONFactory.setDefaultWriterAlphabetic(false);
            Bean2 bean = new Bean2();
            bean.a1 = 1;
            bean.a2 = 2;
            bean.a3 = 3;
            assertEquals("{\"a2\":2,\"a1\":1,\"a3\":3}", JSON.toJSONString(bean));
        } finally {
            JSONFactory.setDefaultWriterAlphabetic(defaultWriterAlphabetic);
        }
    }

    public static class Bean2 {
        public int a2;
        public int a1;
        public int a3;
    }
}
