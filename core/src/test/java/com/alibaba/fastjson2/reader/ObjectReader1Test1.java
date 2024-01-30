package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectReader1Test1 {
    @Test
    public void test() {
        String str = JSONObject
                .of()
                .fluentPut("f01", 1)
                .toString();

        {
            Bean bean = JSON.parseObject(str, Bean.class);
            assertEquals(1, bean.f01);
        }
        {
            Bean bean = JSON.parseObject(str).to(Bean.class);
            assertEquals(1, bean.f01);
        }
        {
            Bean1 bean = JSON.parseObject(str, Bean1.class);
            assertEquals(1, bean.f01);
        }
        {
            Bean2 bean = JSON.parseObject(str, Bean2.class);
            assertEquals(1, bean.f01);
        }
    }

    @Test
    public void test1() {
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        String[] fieldNames = new String[] {
                "f01"
        };
        for (String fieldName : fieldNames) {
            assertEquals(fieldName,
                    objectReader.getFieldReader(fieldName).fieldName);
            assertEquals(fieldName,
                    objectReader.getFieldReader(fieldName.toUpperCase()).fieldName);
        }
        assertNull(objectReader.getFieldReader("xx"));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    public static class Bean {
        public int f01;
    }

    private static class Bean1 {
        public int f01;
    }

    public static class Bean2 {
        public final int f01;

        @JSONCreator
        public Bean2(int f01) {
            this.f01 = f01;
        }
    }
}
