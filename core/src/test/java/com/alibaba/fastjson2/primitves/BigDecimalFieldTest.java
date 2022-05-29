package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BigDecimalField1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BigDecimalField1> objectWriter = creator.createObjectWriter(BigDecimalField1.class);

            {
                BigDecimalField1 vo = new BigDecimalField1();
                vo.id = BigDecimal.valueOf(1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":1}", jsonWriter.toString());
            }
            {
                BigDecimalField1 vo = new BigDecimalField1();
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                BigDecimalField1 vo = new BigDecimalField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}", jsonWriter.toString());
            }
            {
                BigDecimalField1 vo = new BigDecimalField1();
                vo.id = BigDecimal.valueOf(1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                BigDecimalField1 vo = new BigDecimalField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("id", 0, 0, null, BigDecimalField1.class.getField("id"));
            ObjectWriter<BigDecimalField1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                BigDecimalField1 vo = new BigDecimalField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                BigDecimalField1 vo = new BigDecimalField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                BigDecimalField1 vo = new BigDecimalField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            BigDecimalField1 vo = new BigDecimalField1();

            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            JSONPath jsonPath = JSONPath
                    .of("$.id")
                    .setReaderContext(readContext);
            jsonPath.set(vo, 101);
            assertEquals(BigDecimal.valueOf(101), vo.id);
            jsonPath.set(vo, 102L);
            assertEquals(BigDecimal.valueOf(102), vo.id);

            jsonPath.set(vo, null);
            assertEquals(null, vo.id);

            jsonPath.set(vo, "103");
            assertEquals(BigDecimal.valueOf(103), vo.id);
            assertEquals(BigDecimal.valueOf(103), jsonPath.eval(vo));

            jsonPath.setInt(vo, 101);
            assertEquals(BigDecimal.valueOf(101), vo.id);
            jsonPath.setLong(vo, 102L);
            assertEquals(BigDecimal.valueOf(102), vo.id);

            JSONPath
                    .of("$.v0000")
                    .setReaderContext(readContext)
                    .setInt(vo, 103);
            assertEquals(BigDecimal.valueOf(102), vo.id);
        }
    }
}
