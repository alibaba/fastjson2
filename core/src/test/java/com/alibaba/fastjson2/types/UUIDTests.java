package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UUIDTests {
    @Test
    public void test() throws Exception {
        UUID uuid = UUID.randomUUID();
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter("value", UUID.class, Bean::getValue)
        );

        String str = "{\"value\":\"" + uuid + "\"}";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = uuid;
            objectWriter.write(jsonWriter, bean);
            assertEquals(str, jsonWriter.toString());

            assertNotNull(objectWriter.getFieldWriter("value").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.value = uuid;
            objectWriter.write(jsonWriter, bean);
            Bean bean1 = JSONB.parseObject(jsonWriter.getBytes(), Bean.class);
            assertEquals(bean.value, bean1.value);
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
            bean.value = uuid;
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("\"" + uuid + "\"", jsonWriter.toString());
        }

        {
            ObjectReader<Bean> objectReader = ObjectReaders.objectReader(
                    Bean.class,
                    Bean::new,
                    ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean.class.getMethod("setValue", UUID.class))
            );

            Bean object = objectReader.readObject(JSONReader.of(str));
            assertEquals(uuid, object.value);

            FieldReader fieldReader = objectReader.getFieldReader("value");
            assertNotNull(fieldReader.getObjectReader(JSONReader.of(str)));
            assertNotNull(fieldReader.getObjectReader(JSONFactory.createReadContext()));
        }
    }

    public static class Bean {
        private UUID value;

        public UUID getValue() {
            return value;
        }

        public void setValue(UUID value) {
            this.value = value;
        }
    }
}
