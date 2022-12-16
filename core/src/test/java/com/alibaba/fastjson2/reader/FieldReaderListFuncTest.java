package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldReaderListFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("values");
        assertNotNull(fieldReader.method);
        assertEquals(Long.class, fieldReader.getItemType());

        fieldReader.accept(
                bean,
                Arrays.asList(Long.valueOf(123L))
        );
        assertEquals(123L, bean.values.get(0));

        assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101L,
                objectReader.readObject(
                        JSONReader.of("{\"values\":[101]}"),
                        0
                ).values.get(0)
        );
    }

    @Test
    public void test_reflect() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("values");
        assertNotNull(fieldReader.method);
        assertEquals(Long.class, fieldReader.getItemType());

        fieldReader.accept(
                bean,
                Arrays.asList(Long.valueOf(123L))
        );
        assertEquals(123L, bean.values.get(0));

        assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101L,
                objectReader.readObject(
                        JSONReader.of("{\"values\":[101]}"),
                        0
                ).values.get(0)
        );
    }

    public static class Bean {
        private List<Long> values;

        public List<Long> getValues() {
            return values;
        }

        public void setValues(List<Long> values) {
            this.values = values;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1(123);
        ObjectReader<Bean1> objectReader = TestUtils.createObjectReaderLambda(Bean1.class);
        FieldReader fieldReader = objectReader.getFieldReader("values");
        assertNotNull(fieldReader.method);
        assertEquals(Long.class, fieldReader.getItemType());

        fieldReader.accept(
                bean,
                Arrays.asList(Long.valueOf(123L))
        );
        assertEquals(123L, bean.values.get(0));

        assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                102L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":123,\"values\":[101,102]}"),
                        0
                ).values.get(1)
        );
    }

    @Test
    public void test1_jsonb() {
        JSONObject object = JSONObject.of("id", 123, "values", Arrays.asList(101L, 102L));
        byte[] jsonbBytes = JSONB.toBytes(object);
        ObjectReader<Bean1> objectReader = TestUtils.createObjectReaderLambda(Bean1.class);
        Bean1 bean1 = objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes), null, null, 0);
        assertEquals(101L, bean1.values.get(0));
        assertEquals(102L, bean1.values.get(1));
    }

    public static class Bean1 {
        private int id;
        private List<Long> values;

        public Bean1(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public List<Long> getValues() {
            return values;
        }

        public void setValues(List<Long> values) {
            this.values = values;
        }
    }
}
