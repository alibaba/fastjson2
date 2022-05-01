package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

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
}
