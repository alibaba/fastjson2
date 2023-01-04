package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectReadersTest {
    @Test
    public void test() {
        ObjectReader objectReader = ObjectReaders.objectReader(
                new ColorCreator(),
                fieldReader("rgb", int.class),
                fieldReader("r", int.class),
                fieldReader("g", int.class),
                fieldReader("b", int.class)
        );
        java.awt.Color color = (java.awt.Color) objectReader.readObject(JSONReader.of("{\"rgb\":0}"));
        assertEquals(0xff000000, color.getRGB());
    }

    static class ColorCreator
            implements Function<Map<Long, Object>, java.awt.Color> {
        static final long HASH_RGB = Fnv.hashCode64("rgb");
        static final long HASH_R = Fnv.hashCode64("r");
        static final long HASH_G = Fnv.hashCode64("g");
        static final long HASH_B = Fnv.hashCode64("b");

        @Override
        public java.awt.Color apply(Map<Long, Object> values) {
            Integer rgb = (Integer) values.get(HASH_RGB);
            if (rgb != null) {
                return new java.awt.Color(rgb.intValue());
            }

            Integer r = (Integer) values.get(HASH_R);
            Integer g = (Integer) values.get(HASH_G);
            Integer b = (Integer) values.get(HASH_B);
            return new java.awt.Color(r, g, b);
        }
    }

    @Test
    public void test1() {
        FieldReader fieldReader = fieldReader("id", Long.class, Long.class);
        assertEquals(123L, fieldReader.readFieldValue(JSONReader.of("123")));
    }

    @Test
    public void test2() {
        ObjectReader<Bean2> objectReader = ObjectReaders.objectReader(
                Bean2.class,
                Bean2::new,
                fieldReaderBool("value", Bean2::setValue)
        );
        Bean2 bean2 = objectReader.readObject(JSONReader.of("{\"value\":1}"));
        assertTrue(bean2.value);
    }

    static class Bean2 {
        private boolean value;

        public boolean isValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = ObjectReaders.objectReader(
                Bean3.class,
                Bean3::new,
                fieldReaderChar("value", Bean3::setValue)
        );
        Bean3 bean = objectReader.readObject(JSONReader.of("{\"value\":\"A\"}"));
        assertEquals('A', bean.value);
    }

    static class Bean3 {
        private char value;

        public char getValue() {
            return value;
        }

        public void setValue(char value) {
            this.value = value;
        }
    }

    @Test
    public void test4() {
        ObjectReader<Bean4> objectReader = ObjectReaders.objectReader(
                Bean4.class,
                Bean4::new,
                fieldReaderFloat("value", Bean4::setValue)
        );
        Bean4 bean = objectReader.readObject(JSONReader.of("{\"value\":\"12.34\"}"));
        assertEquals(12.34F, bean.value);
    }

    static class Bean4 {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    @Test
    public void test5() {
        ObjectReader<Bean5> objectReader = ObjectReaders.objectReader(
                Bean5.class,
                Bean5::new,
                fieldReader("value", String.class, String.class, Bean5::setValue)
        );
        Bean5 bean = objectReader.readObject(JSONReader.of("{\"value\":\"12.34\"}"));
        assertEquals("12.34", bean.value);
    }

    @Test
    public void testString() {
        ObjectReader<Bean5> objectReader = ObjectReaders.objectReader(
                Bean5.class,
                Bean5::new,
                fieldReaderString("value", Bean5::setValue)
        );
        Bean5 bean = objectReader.readObject(JSONReader.of("{\"value\":\"12.34\"}"));
        assertEquals("12.34", bean.value);
    }

    static class Bean5 {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
