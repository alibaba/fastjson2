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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalDateTests {
    @Test
    public void test() throws Exception {
        LocalDate dateTime = LocalDate.of(2001, 2, 3);
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter("value", LocalDate.class, Bean::getValue)
        );

        String str = "{\"value\":\"" + dateTime + "\"}";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = dateTime;
            objectWriter.write(jsonWriter, bean);
            assertEquals(str, jsonWriter.toString());

            assertNotNull(objectWriter.getFieldWriter("value").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.value = dateTime;
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
            bean.value = dateTime;
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("\"" + dateTime + "\"", jsonWriter.toString());
        }

        {
            ObjectReader<Bean> objectReader = ObjectReaders.objectReader(
                    Bean.class,
                    Bean::new,
                    ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean.class.getMethod("setValue", LocalDate.class))
            );

            Bean object = objectReader.readObject(JSONReader.of(str));
            assertEquals(dateTime, object.value);

            FieldReader fieldReader = objectReader.getFieldReader("value");
            assertNotNull(fieldReader.getObjectReader(JSONReader.of(str)));
            assertNotNull(fieldReader.getObjectReader(JSONFactory.createReadContext()));
        }
    }

    public static class Bean {
        private LocalDate value;

        public LocalDate getValue() {
            return value;
        }

        public void setValue(LocalDate value) {
            this.value = value;
        }
    }
}
