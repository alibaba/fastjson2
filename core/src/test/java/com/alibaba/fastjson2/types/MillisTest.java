package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MillisTest {
    @Test
    public void test1() throws Exception {
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);

        long millis = 1706767932876L;
        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = millis;
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"value\":\"2024-02-01 14:12:12\"}", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{}", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"value\":null}", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = millis;
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("\"2024-02-01 14:12:12\"", jsonWriter.toString());
        }
    }

    private static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
