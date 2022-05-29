package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest22 {
    @Test
    public void test_1() throws Exception {
        XItem item = new XItem(101);
        Bean1 bean = new Bean1();
        bean.item = item;
        bean.item1 = item;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.item);
        assertNotNull(bean2.item1);

        assertSame(bean2.item, bean2.item1);
    }

    public static class Bean1 {
        Item item;
        Item item1;
    }

    public abstract static class Item {
        public int id;
    }

    public static class XItem
            extends Item {
        public XItem(int id) {
            this.id = id;
        }
    }
}
