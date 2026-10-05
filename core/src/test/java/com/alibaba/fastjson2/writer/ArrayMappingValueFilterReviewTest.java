package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayMappingValueFilterReviewTest {
    @Test
    public void arrayMappingWritesReplacementValueAndType() {
        ObjectWriter<Bean> objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.BeanToArray);
        context.setValueFilter((object, name, value) -> "replacement");
        try (JSONWriter writer = JSONWriter.of(context)) {
            objectWriter.write(writer, new Bean());
            assertEquals("[\"replacement\"]", writer.toString());
        }
    }

    public static class Bean {
        public int value = 123;
    }
}
