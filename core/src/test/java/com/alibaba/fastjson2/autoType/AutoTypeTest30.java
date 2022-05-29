package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AutoTypeTest30 {
    @Test
    public void test_0() throws Exception {
        List list = new ArrayList<>();
        list.add("101");
        Bean1 bean = new Bean1();
        bean.items = list;

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );
        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.get(0), bean2.items.get(0));
    }

    @Test
    public void test_beanList() throws Exception {
        List list = new ArrayList<>();
        {
            Bean1 bean = new Bean1();
            bean.items = new ArrayList<>();
            bean.items.add("101");
            list.add(bean);
        }
        {
            Bean1 bean = new Bean1();
            bean.items = new ArrayList<>();
            bean.items.add("102");
            list.add(bean);
        }
        {
            Bean1 bean = new Bean1();
            bean.items = new ArrayList<>();
            bean.items.add("103");
            list.add(bean);
        }

        byte[] bytes = JSONB.toBytes(list,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        List list2 = (List) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        Bean1 bean = (Bean1) list.get(0);
        Bean1 bean2 = (Bean1) list2.get(0);

        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
        assertEquals(bean.items.size(), bean2.items.size());
        assertEquals(bean.items.get(0), bean2.items.get(0));
    }

    public static class Bean1 {
        private List<String> items;
    }

    @Test
    public void test_2() throws Exception {
        Bean2 bean = new Bean2();
        bean.value = "1001";

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        assertEquals("{\n" +
                        "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest30$Bean2\",\n" +
                        "\t\"value\":\"1001\"\n" +
                        "}",
                JSONB.toJSONString(bytes));

        Bean2 bean2 = (Bean2) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );
        assertNotNull(bean2);
        assertNotNull(bean2.value);

        assertSame(bean.value.getClass(), bean2.value.getClass());
    }

    public static class Bean2
            extends Address {
        private String value;
    }

    public static class Address
            extends BaseAddress {
    }

    public static class BaseAddress {
        private String value;
    }
}
