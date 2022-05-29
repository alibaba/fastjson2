package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.util.JSONBDump;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.List1;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class List1Test {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<List1> objectWriter = creator.createObjectWriter(List1.class);

            {
                List1 vo = new List1();
                vo.setV0000(Collections.singletonList(1));
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":[1]}", jsonWriter.toString());
            }
            {
                List1 vo = new List1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                List1 vo = new List1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                List1 vo = new List1();
                vo.setV0000(Collections.singletonList(1));
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[[1]]", jsonWriter.toString());
            }
            {
                List1 vo = new List1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }

            {
                List1 vo = new List1();
                vo.setV0000(Collections.singletonList(1));
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                JSONBDump.dump(jsonbBytes);
                assertEquals("{\n" +
                        "\t\"v0000\":[1]\n" +
                        "}", JSONB.toJSONString(jsonbBytes));
            }
            {
                List1 vo = new List1();
                vo.setV0000(Collections.singletonList(1));
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[\n" +
                        "\t[1]\n" +
                        "]", JSONB.toJSONString(jsonbBytes));
            }
            {
                List1 vo = new List1();
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[null]", JSONB.toJSONString(jsonbBytes));
            }
        }
    }

    @Test
    public void test_list() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            String str = "{\"v0000\":[1001]}";
            ObjectReader<List1> objectReader = creator.createObjectReader(List1.class);
            List1 vo = objectReader.readObject(JSONReader.of(str), 0);
            assertNotNull(vo.getV0000());
            assertEquals(1, vo.getV0000().size());
            assertEquals(Integer.valueOf(1001), vo.getV0000().get(0));
        }
    }

    @Test
    public void test_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeAny(Collections.singletonMap("v0000", Arrays.asList(1001)));
            byte[] jsonbBytes = jsonWriter.getBytes();
            ObjectReader<List1> objectReader = creator.createObjectReader(List1.class);
            List1 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertNotNull(vo.getV0000());
            assertEquals(1, vo.getV0000().size());
            assertEquals(Integer.valueOf(1001), vo.getV0000().get(0));
        }
    }
}
