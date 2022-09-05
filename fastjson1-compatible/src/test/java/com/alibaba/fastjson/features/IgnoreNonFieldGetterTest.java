package com.alibaba.fastjson.features;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreNonFieldGetterTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{\"id\":123}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, SerializerFeature.IgnoreNonFieldGetter));
    }

    public static class Bean {
        public int getId() {
            return 123;
        }
    }
}
