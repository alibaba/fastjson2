package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderInt16ValueMethodTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123, bean.value);
        assertNotNull(fieldReader.method);

        fieldReader.accept(bean, (short) 101);
        assertEquals(101, bean.value);

        fieldReader.accept(bean, 102);
        assertEquals(102, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );

        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes));
        assertEquals(bean.value, bean1.value);
    }

    public static class Bean {
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        ObjectReader<Bean1> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean1.class);

        assertEquals(
                (short) 101,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean1 {
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    public static class Bean2 {
        public void setValue(short value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean3.class);
        assertEquals(
                (short) 123,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );
    }

    private static class Bean3 {
        private short value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }
}
