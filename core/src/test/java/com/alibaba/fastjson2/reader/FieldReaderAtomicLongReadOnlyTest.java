package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldReaderAtomicLongReadOnlyTest {
    @Test
    public void test() {
        String str = "{\"value\":123}";
        assertEquals(
                123,
                JSON.parseObject(str, Bean.class).value.get()
        );

        assertEquals(
                123,
                JSON.parseObject(str).to(Bean.class).value.get()
        );

        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals("getValue", fieldReader.toString());
        assertTrue(fieldReader.isReadOnly());
    }

    public static class Bean {
        private final AtomicLong value = new AtomicLong();

        public AtomicLong getValue() {
            return value;
        }
    }

    @Test
    public void test1() {
        String str = "{\"id\":101,\"value\":123}";
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(
                101,
                bean1.id
        );
        assertEquals(
                123,
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
                0,
                bean1.value.get()
        );
    }

    public static class Bean1 {
        private final int id;
        private final AtomicLong value = new AtomicLong();

        public Bean1(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public AtomicLong getValue() {
            return value;
        }
    }
}
