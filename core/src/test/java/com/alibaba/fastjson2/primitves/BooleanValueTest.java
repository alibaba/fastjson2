package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BooleanValue1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanValueTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BooleanValue1> objectWriter = creator.createObjectWriter(BooleanValue1.class);

            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":true}", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":false}", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":true}", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":false}", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[true]", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[false]", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[true]", jsonWriter.toString());
            }
            {
                BooleanValue1 vo = new BooleanValue1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[false]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read_0() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<BooleanValue1> objectWriter = creator.createObjectReader(BooleanValue1.class);
            {
                BooleanValue1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":1}"), 0);
                assertEquals(true, vo.isV0000());
            }
            {
                BooleanValue1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":false}"), 0);
                assertEquals(false, vo.isV0000());
            }
            {
                BooleanValue1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":\"true\"}"), 0);
                assertEquals(true, vo.isV0000());
            }
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            BooleanValue1 vo = new BooleanValue1();

            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            JSONPath jsonPath = JSONPath
                    .of("$.v0000")
                    .setReaderContext(readContext);
            jsonPath.set(vo, true);
            assertEquals(true, vo.isV0000());
            jsonPath.set(vo, null);
            assertEquals(false, vo.isV0000());
            jsonPath.set(vo, false);
            assertEquals(false, vo.isV0000());
        }
    }
}
