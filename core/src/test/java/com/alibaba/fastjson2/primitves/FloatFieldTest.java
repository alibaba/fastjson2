package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.FloatField1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<FloatField1> objectWriter = creator.createObjectWriter(FloatField1.class);

            {
                FloatField1 vo = new FloatField1();
                vo.v0000 = 1F;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1.0}", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                vo.v0000 = 1F;
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1.0}", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                vo.v0000 = 1F;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1.0]", jsonWriter.toString());
            }
            {
                FloatField1 vo = new FloatField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<FloatField1> objectWriter = creator.createObjectReader(FloatField1.class);
            {
                FloatField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":1}"), 0);
                assertEquals(1F, vo.v0000);
            }
            {
                FloatField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":0}"), 0);
                assertEquals(0F, vo.v0000);
            }
            {
                FloatField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":\"1.0\"}"), 0);
                assertEquals(1F, vo.v0000);
            }
        }
    }
}
