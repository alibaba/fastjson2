package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderCharValueFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.READER_CREATOR_LAMBDA.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "A");
        assertEquals('A', bean.value);
        assertNotNull(fieldReader.getMethod());

        fieldReader.accept(bean, 'B');
        assertEquals('B', bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                'A',
                objectReader.readObject(
                        JSONReader.of("{\"value\":\"A\"}"),
                        0
                ).value
        );
    }

    public static class Bean {
        private char value;

        public char getValue() {
            return value;
        }

        public void setValue(char value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = ObjectReaderCreatorLambda.INSTANCE.createObjectReader(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(UnsupportedOperationException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, (short) 123));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123D));
    }

    public static class Bean2 {
        public void setValue(char value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = ObjectReaderCreatorLambda.INSTANCE.createObjectReader(Bean3.class);
        assertEquals(
                'A',
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"A\"}")
                ).value
        );
    }

    public static class Bean3 {
        private char value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public char getValue() {
            return value;
        }

        public void setValue(char value) {
            this.value = value;
        }
    }
}
