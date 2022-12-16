package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderBooleanValueFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, true);
        assertEquals(true, bean.value);
        assertNotNull(fieldReader.field);

        fieldReader.accept(bean, 0);
        assertEquals(false, bean.value);

        fieldReader.accept(bean, 1);
        assertEquals(true, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                true,
                objectReader.readObject(
                        JSONReader.of("{\"value\":true}"),
                        0
                ).value
        );
    }

    private static class Bean {
        public boolean value;
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean3.class);
        assertEquals(
                true,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"true\"}")
                ).value
        );
    }

    private static class Bean3 {
        public boolean value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
