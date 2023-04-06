package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static com.alibaba.fastjson2.writer.ObjectWriters.*;
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

    @Test
    public void testToString() {
        ObjectWriter objectWriter = ObjectWriterCreator.ofToString(Bean::toString);

        Bean bean = new Bean();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("\"101\"", jsonWriter.toString());
    }

    @Test
    public void testToInt() {
        ObjectWriter<Bean> objectWriter = ObjectWriters.ofToInt(
                (ToIntFunction<Bean>) Bean::getId
        );

        Bean bean = new Bean();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("101", jsonWriter.toString());
    }

    public static class Bean {
        public int id;

        public String toString() {
            return Integer.toString(id);
        }

        public int getId() {
            return id;
        }
    }

    @Test
    public void testToLong() {
        ObjectWriter<Bean2> objectWriter = ObjectWriters.ofToLong(
                (ToLongFunction<Bean2>) Bean2::getId
        );

        Bean2 bean = new Bean2();
        bean.id = 101;

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("101", jsonWriter.toString());
    }

    public static class Bean2 {
        public long id;

        public long getId() {
            return id;
        }
    }

    @Test
    public void test3() throws Exception {
        ObjectWriter<Bean3> objectWriter = ObjectWriters.of(
                Bean3.class,
                fieldWriterListString("names", Bean3::getNames)
        );

        Bean3 bean = new Bean3();
        bean.names = new ArrayList<>();
        bean.names.add("1");
        bean.names.add("2");
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"names\":[\"1\",\"2\"]}", jsonWriter.toString());
    }

    @Test
    public void test3_1() throws Exception {
        ObjectWriter<Bean3> objectWriter = ObjectWriters.of(
                Bean3.class,
                fieldWriterList("names", String.class, Bean3::getNames)
        );

        Bean3 bean = new Bean3();
        bean.names = new ArrayList<>();
        bean.names.add("1");
        bean.names.add("2");
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"names\":[\"1\",\"2\"]}", jsonWriter.toString());
    }

    @Test
    public void test3_2() throws Exception {
        ObjectWriter<Bean3> objectWriter = ObjectWriters.of(
                Bean3.class,
                fieldWriter("names", TypeUtils.PARAM_TYPE_LIST_STR, List.class, Bean3::getNames)
        );

        Bean3 bean = new Bean3();
        bean.names = new ArrayList<>();
        bean.names.add("1");
        bean.names.add("2");
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"names\":[\"1\",\"2\"]}", jsonWriter.toString());
    }

    public static class Bean3 {
        private List<String> names;

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }
    }

    @Test
    public void test4() throws Exception {
        ObjectWriter<Bean4> objectWriter = ObjectWriters.of(
                Bean4.class,
                fieldWriterList("names", Long.class, Bean4::getNames)
        );

        Bean4 bean = new Bean4();
        bean.names = new ArrayList<>();
        bean.names.add(1L);
        bean.names.add(2L);
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"names\":[1,2]}", jsonWriter.toString());
    }

    public static class Bean4 {
        private List<Long> names;

        public List<Long> getNames() {
            return names;
        }

        public void setNames(List<Long> names) {
            this.names = names;
        }
    }

    @Test
    public void testFloat() throws Exception {
        ObjectWriter<Bean5> objectWriter = ObjectWriters.of(
                Bean5.class,
                fieldWriter("value", Bean5::getValue)
        );

        Bean5 bean = new Bean5();
        bean.value = 12.0F;
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"value\":12.0}", jsonWriter.toString());
    }

    public static class Bean5 {
        private Float value;

        public Float getValue() {
            return value;
        }
    }

    @Test
    public void testDouble() throws Exception {
        ObjectWriter<Bean6> objectWriter = ObjectWriters.of(
                Bean6.class,
                fieldWriter("value", Bean6::getValue)
        );

        Bean6 bean = new Bean6();
        bean.value = 12.0D;
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"value\":12.0}", jsonWriter.toString());
    }

    public static class Bean6 {
        private Double value;

        public Double getValue() {
            return value;
        }
    }

    @Test
    public void testBoolean() throws Exception {
        ObjectWriter<Bean7> objectWriter = ObjectWriters.of(
                Bean7.class,
                fieldWriter("value", Bean7::getValue)
        );

        Bean7 bean = new Bean7();
        bean.value = true;
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"value\":true}", jsonWriter.toString());
    }

    @Test
    public void testBoolean_1() throws Exception {
        ObjectWriter<Bean7> objectWriter = ObjectWriters.objectWriter(
                Bean7.class,
                0,
                fieldWriter("value", Bean7::getValue)
        );

        Bean7 bean = new Bean7();
        bean.value = true;
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        assertEquals("{\"value\":true}", jsonWriter.toString());
    }

    public static class Bean7 {
        private Boolean value;

        public Boolean getValue() {
            return value;
        }
    }
}
