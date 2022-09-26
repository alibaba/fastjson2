package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.writer.ObjectWriters.fieldWriter;
import static com.alibaba.fastjson2.writer.ObjectWriters.objectWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectWritersTest {
    @Test
    public void test() {
        ObjectWriter objectWriter = objectWriter(
                fieldWriter("id", (Bean o) -> o.id)
        );

        Bean bean = new Bean();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"id\":101}", jsonWriter.toString());
    }

    @Test
    public void test1() {
        ObjectWriter objectWriter = objectWriter(
                Bean.class,
                fieldWriter("id", (Bean o) -> o.id)
        );

        Bean bean = new Bean();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"id\":101}", jsonWriter.toString());
    }

    @Test
    public void test2() {
        ObjectWriter objectWriter = ObjectWriters.of(
                Bean.class,
                fieldWriter("id", (Bean o) -> o.id)
        );

        Bean bean = new Bean();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"id\":101}", jsonWriter.toString());
    }

    public static class Bean {
        public int id;
    }
}
