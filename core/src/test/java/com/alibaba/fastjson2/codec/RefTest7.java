package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RefTest7 {
    @Test
    public void test_ref_0() {
        Value value = new Value();
        Bean bean = new Bean();
        bean.values = new HashMap<>();
        bean.values1 = new HashMap<>();
        bean.values.put(new Key(), value);
        bean.values1.put(new Key(), value);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );
        assertNotNull(bean2);
        assertNotNull(bean2.values);

        Assertions.assertSame(bean.values.getClass(), bean2.values.getClass());
        Assertions.assertSame(bean2.values1.entrySet().iterator().next().getValue(), bean2.values.entrySet().iterator().next().getValue());
    }

    public static class Bean {
        Map<Key, Value> values;
        Map<Key, Value> values1;
    }

    public static class Value {
    }

    public static class Key {
        public int id;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return id == key.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
