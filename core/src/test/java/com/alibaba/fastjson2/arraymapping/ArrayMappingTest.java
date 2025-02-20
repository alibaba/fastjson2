package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayMappingTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Int1> objectWriter
                    = creator.createObjectWriter(Int1.class);

            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.config(JSONWriter.Feature.BeanToArray);
            Int1 vo = new Int1();
            vo.setV0000(101);
            objectWriter.write(jsonWriter, vo);
            String json = jsonWriter.toString();
            assertEquals("[101]", json);
        }
    }

    @Test
    public void test_int1_json() {
        Int1 vo = new Int1();
        vo.setV0000(123);
        byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123]", new String(bytes));
    }

    @Test
    public void test_long1_json() {
        LongValue1 vo = new LongValue1();
        vo.setV0000(123);
        byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123]", new String(bytes));
    }

    @Test
    public void test_Integer1_json() {
        Integer1 vo = new Integer1();

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
            assertEquals("[null]", new String(bytes));
        }

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.NullAsDefaultValue);
            assertEquals("[0]", new String(bytes));
        }

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteNullNumberAsZero);
            assertEquals("[0]", new String(bytes));
        }

        vo.setV0000(123);
        byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123]", new String(bytes));
    }

    @Test
    public void test_Long1_json() {
        Long1 vo = new Long1();

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
            assertEquals("[null]", new String(bytes));
        }

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.NullAsDefaultValue);
            assertEquals("[0]", new String(bytes));
        }

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteNullNumberAsZero);
            assertEquals("[0]", new String(bytes));
        }

        vo.setV0000(123L);
        byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123]", new String(bytes));
    }

    @Test
    public void test_String1_json() {
        String1 vo = new String1();

        {
            byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
            assertEquals("[null]", new String(bytes));
        }

        vo.setId("abc");
        byte[] bytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[\"abc\"]", new String(bytes));
    }

    @Test
    public void test_arrayMapping_1() {
        String[] strings = {"123", "1234中"};
        for (String string : strings) {
            String1 bean = new String1();
            bean.setId(string);
            byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
            String1 parsed = JSONB.parseObject(bytes, String1.class, JSONReader.Feature.SupportArrayToBean);
            assertEquals(bean.getId(), parsed.getId());
        }
    }

    @Test
    public void test_arrayMapping_2() {
        Bean2 bean = new Bean2();
        bean.values = new ArrayList<>();
        bean.values.add(1L);
        bean.values.add(2L);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean2 parsed = JSONB.parseObject(bytes, Bean2.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(bean.values.size(), parsed.values.size());
    }

    public static class Bean2 {
        public List<Long> values;
    }

    @Test
    public void test_2() {
        ListStr1 vo = new ListStr1();
        vo.setV0000(Collections.singletonList("1"));
        byte[] jsonbBytes = JSONB.toBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[\n" +
                "\t[\"1\"]\n" +
                "]", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test_2_json() {
        ListStr1 vo = new ListStr1();
        vo.setV0000(Collections.singletonList("1"));
        byte[] jsonbBytes = JSON.toJSONBytes(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[[\"1\"]]", new String(jsonbBytes));
    }

    @Test
    public void test_3() {
        Bean vo = new Bean();
        vo.values.add(new Int1());
        {
            byte[] jsonbBytes = JSONB.toBytes(vo, JSONWriter.Feature.BeanToArray);
            assertEquals("[\n" +
                    "\t[\n" +
                    "\t\t[0]\n" +
                    "\t]\n" +
                    "]", JSONB.toJSONString(jsonbBytes));
        }
    }

    public class Bean {
        private List<Int1> values = new ArrayList<>();

        public List<Int1> getValues() {
            return values;
        }
    }
}
