package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Differ;
import org.junit.jupiter.api.Test;

public class AutoTypeTest12 {
    @Test
    public void test_0() throws Exception {
        Bean bean = new Bean();
        bean.item = bean.new Item();

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);

        Differ.diff(bean, bean2);
    }

    public static class Bean {
        public Item item;

        public class Item {
        }
    }

//    @Test
//    public void test4() {
//        Object o = JSON.parseObject("{\"@type\":\"org.springframework.cache.support.NullValue\"}",
//                Object.class, Feature.SupportAutoType
//        );
//
//        assertNotNull(o.getClass());
//    }
}
