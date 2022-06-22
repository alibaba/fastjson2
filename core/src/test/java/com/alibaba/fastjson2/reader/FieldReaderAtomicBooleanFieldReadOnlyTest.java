package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldReaderAtomicBooleanFieldReadOnlyTest {
    @Test
    public void test() {
        String str = "{\"value\":true}";
        assertEquals(
                true,
                JSON.parseObject(str, Bean.class).value.get()
        );

        assertEquals(
                true,
                JSON.parseObject(str).to(Bean.class).value.get()
        );

        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals("value", fieldReader.toString());
        assertTrue(fieldReader.isReadOnly());
    }

    private static class Bean {
        public final AtomicBoolean value = new AtomicBoolean();
    }

    @Test
    public void test1() {
        String str = "{\"id\":101,\"value\":true}";
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(
                101,
                bean1.id
        );
        assertEquals(
                true,
                bean1.value.get()
        );
    }

    @Test
    public void test1_1() {
        String str = "{\"id\":101,\"value\":null}";
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(
                101,
                bean1.id
        );
        assertEquals(
                false,
                bean1.value.get()
        );
    }

    private static class Bean1 {
        private final int id;
        public final AtomicBoolean value = new AtomicBoolean();

        public Bean1(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
