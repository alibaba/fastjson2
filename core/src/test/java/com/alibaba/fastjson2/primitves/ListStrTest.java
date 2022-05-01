package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.ListStr1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ListStrTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<ListStr1> objectWriter = creator.createObjectWriter(ListStr1.class);

            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(Collections.singletonList("1"));
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":[\"1\"]}", jsonWriter.toString());
            }
            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(Collections.singletonList("1"));
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[[\"1\"]]", jsonWriter.toString());
            }
            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }

            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(Collections.singletonList("1"));
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("{\n" +
                        "\t\"v0000\":[\"1\"]\n" +
                        "}", JSONB.toJSONString(jsonbBytes));
            }
            {
                ListStr1 vo = new ListStr1();
                vo.setV0000(Collections.singletonList("1"));
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[\n" +
                        "\t[\"1\"]\n" +
                        "]", JSONB.toJSONString(jsonbBytes));
            }
            {
                ListStr1 vo = new ListStr1();
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[null]", JSONB.toJSONString(jsonbBytes));
            }
        }
    }

    @Test
    public void test_0() {
        String str = "[1,2,3]";
        Type type = new TypeReference<List<String>>() {
        }.getType();
        List<String> array = JSON.parseObject(str, type);
        assertEquals("1", array.get(0));

        JSONWriter jsonWriter = JSONWriter.of();
        ObjectWriter objectWriter = JSONFactory
                .getDefaultObjectWriterProvider()
                .getObjectWriter(type, List.class);
        objectWriter.write(jsonWriter, array);
        assertEquals("[\"1\",\"2\",\"3\"]", jsonWriter.toString());
    }

    @Test
    public void test_list() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            String str = "{\"v0000\":[1001]}";
            ObjectReader<ListStr1> objectReader = creator.createObjectReader(ListStr1.class);
            ListStr1 vo = objectReader.readObject(JSONReader.of(str), 0);
            assertNotNull(vo.getV0000());
            assertEquals(1, vo.getV0000().size());
            assertEquals("1001", vo.getV0000().get(0));
        }
    }

    @Test
    public void test_jsonb() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeAny(Collections.singletonMap("v0000", Arrays.asList(1001)));
        byte[] jsonbBytes = jsonWriter.getBytes();
        ObjectReader<ListStr1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(ListStr1.class);
        ListStr1 vo = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes), 0);
        assertNotNull(vo.getV0000());
        assertEquals(1, vo.getV0000().size());
        assertEquals("1001", vo.getV0000().get(0));
    }
}
