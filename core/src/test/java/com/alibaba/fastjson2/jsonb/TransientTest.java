package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter.Feature;
import com.alibaba.fastjson2.util.UnsafeUtils;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNull;

public class TransientTest {
    @Test
    public void test_for_transient() throws Exception {
        Bean test = new Bean();
        byte[] bytes = JSONB.toBytes(test, Feature.WriteClassName,
                Feature.FieldBased,
                Feature.ReferenceDetection,
                Feature.WriteNulls,
                Feature.NotWriteDefaultValue,
                Feature.NotWriteHashMapArrayListClassName,
                Feature.WriteNameAsSymbol);
        Bean target = (Bean)JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertNull(target.getAtomicBoolean());

        Bean o = (Bean) UnsafeUtils.UNSAFE.allocateInstance(Bean.class);
        System.out.println(o.getAtomicBoolean());
    }

    static class Bean implements Serializable {
        private transient AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        public AtomicBoolean getAtomicBoolean() {
            return atomicBoolean;
        }
    }
}
