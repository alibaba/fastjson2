package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.FloatValueField1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatValueFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<FloatValueField1> objectWriter = creator.createObjectWriter(FloatValueField1.class);

            {
                FloatValueField1 vo = new FloatValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1.0}", jsonWriter.toString());
            }
            {
                FloatValueField1 vo = new FloatValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1.0]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_1() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    "v0000",
                    float.class,
                    FloatValueField1.class.getField("v0000"));

            ObjectReader<FloatValueField1> objectReader
                    = creator.createObjectReader(FloatValueField1.class, fieldWriter);

            {
                FloatValueField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"v0000\":101}"), 0);
                assertEquals(101F, vo.v0000);
            }
            {
                FloatValueField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"v0000\":null}"), 0);
                assertEquals(0F, vo.v0000);
            }
        }
    }
}
