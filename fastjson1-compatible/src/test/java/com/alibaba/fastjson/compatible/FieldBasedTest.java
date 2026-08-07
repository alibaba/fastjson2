package com.alibaba.fastjson.compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldBasedTest {
    @Test
    public void test() {
        SerializeConfig config = new SerializeConfig(true);
        Bean bean = new Bean();
        bean.userId = 123;
        String str = JSON.toJSONString(bean, config);
        assertEquals("{\"userId\":123}", str);
    }

    public static class Bean {
        private int userId;

        public int getId() {
            throw new UnsupportedOperationException();
        }
    }
}
