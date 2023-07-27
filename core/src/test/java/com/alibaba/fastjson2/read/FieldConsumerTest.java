package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldConsumerTest {
    @Test
    public void test() {
        ObjectReader<Row> objectReader = JSONFactory.getDefaultObjectReaderProvider().createObjectReader(
                new String[]{"id", "name", "value"},
                new Type[]{Integer.class, String.class, Long.class},
                Row::new,
                (o, i, v) -> o.set(i, v)
        );

        JSONReader jsonReader = JSONReader.of("{\"id\":123,\"name\":\"DataWorks\",\"value\":10001}");
        Row row = objectReader.readObject(jsonReader);
        assertEquals(123, row.values[0]);
        assertEquals("DataWorks", row.values[1]);
        assertEquals(10001L, row.values[2]);
    }

    public static class Row {
        Object[] values;

        public Row() {
            values = new Object[3];
        }

        public void set(int index, Object value) {
            values[index] = value;
        }

        public Object get(int index) {
            return values[index];
        }
    }

    @Test
    public void write() {
        ObjectWriter<Row> objectWriter = JSONFactory.getDefaultObjectWriterProvider().getCreator().createObjectWriter(
                new String[]{"id", "name", "value"},
                new Type[]{Integer.class, String.class, Long.class},
                Row::get
        );

        Row row = new Row();
        row.set(0, 123);
        row.set(1, "DataWorks");
        row.set(2, 10001L);

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, row);

        String string = jsonWriter.toString();
        assertEquals("{\"id\":123,\"name\":\"DataWorks\",\"value\":10001}", string);
    }
}
