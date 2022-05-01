package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.ShortField1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<ShortField1> objectWriter = creator.createObjectWriter(ShortField1.class);

            {
                ShortField1 vo = new ShortField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                ShortField1 vo = new ShortField1();
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                ShortField1 vo = new ShortField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                ShortField1 vo = new ShortField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                ShortField1 vo = new ShortField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }
        }
    }
}
