package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.ShortValue1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortValue1Test {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<ShortValue1> objectWriter = creator.createObjectWriter(ShortValue1.class);

            ShortValue1 vo = new ShortValue1();
            vo.setV0000((short) 1);
            {
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
        }
    }
}
