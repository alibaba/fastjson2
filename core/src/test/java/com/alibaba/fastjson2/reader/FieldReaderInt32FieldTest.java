package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldReaderInt32FieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));
        assertEquals(123, objectReader.readObject(JSONReader.of("{\"value\":123}")).value);
    }

    private static class Bean {
        public Integer value;
    }

    @Test
    public void test2() {
        ObjectReader<Bean2> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean2.class);
        assertEquals(
                123,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );
    }

    private static class Bean2 {
        public Integer value;
        public final int id;

        public Bean2(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
