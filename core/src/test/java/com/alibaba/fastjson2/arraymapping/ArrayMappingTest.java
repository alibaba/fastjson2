package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Int1;
import com.alibaba.fastjson2_vo.ListStr1;
import com.alibaba.fastjson2_vo.String1;
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
}
