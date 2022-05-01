package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BooleanField1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BooleanField1> objectWriter = creator.createObjectWriter(BooleanField1.class);

            {
                BooleanField1 vo = new BooleanField1();
                vo.v0000 = true;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":true}", jsonWriter.toString());
            }
            {
                BooleanField1 vo = new BooleanField1();
                vo.v0000 = false;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":false}", jsonWriter.toString());
            }
            {
                BooleanField1 vo = new BooleanField1();
                vo.v0000 = true;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[true]", jsonWriter.toString());
            }
            {
                BooleanField1 vo = new BooleanField1();
                vo.v0000 = false;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[false]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<BooleanField1> objectWriter = creator.createObjectReader(BooleanField1.class);
            {
                BooleanField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":1}"), 0);
                assertEquals(Boolean.TRUE, vo.v0000);
            }
            {
                BooleanField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":false}"), 0);
                assertEquals(Boolean.FALSE, vo.v0000);
            }
            {
                BooleanField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":null}"), 0);
                assertEquals(null, vo.v0000);
            }
            {
                BooleanField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":\"true\"}"), 0);
                assertEquals(Boolean.TRUE, vo.v0000);
            }
        }
    }
}
