package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderCharValueFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "A");
        assertEquals('A', bean.value);
        assertNotNull(fieldReader.field);

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

    private static class Bean {
        public char value;
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean3.class);
        assertEquals(
                'A',
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\"A\"}")
                ).value
        );
    }

    private static class Bean3 {
        public char value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
