package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CharTest {
    @Test
    public void test1() throws Exception {
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter(Bean.class, "value", null, Bean.class.getMethod("getValue"))
        );

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = 'a';
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"value\":\"a\"}", jsonWriter.toString());

            assertNull(objectWriter.getFieldWriter("value").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.value = 'a';
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\n" +
                    "\t\"value\": \"a\"\n" +
                    "}", JSONB.toJSONString(jsonWriter.getBytes()));
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
            bean.value = 'a';
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("\"a\"", jsonWriter.toString());
        }
    }

    private static class Bean {
        private Character value;

        public Character getValue() {
            return value;
        }

        public void setValue(Character value) {
            this.value = value;
        }
    }
}
