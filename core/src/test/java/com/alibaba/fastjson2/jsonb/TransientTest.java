package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransientTest {
    @Test
    public void test_for_transient() {
        Bean bean = new Bean();
        bean.atomicBoolean.set(true);

        byte[] bytes = JSONB.toBytes(bean, Feature.WriteClassName,
                Feature.FieldBased,
                Feature.ReferenceDetection,
                Feature.WriteNulls,
                Feature.NotWriteDefaultValue,
                Feature.NotWriteHashMapArrayListClassName,
                Feature.WriteNameAsSymbol);

        Bean target = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );

        assertTrue(target.getAtomicBoolean() == null || !target.getAtomicBoolean().get());

        Bean target2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseDefaultConstructorAsPossible
        );
        assertNotNull(target2.getAtomicBoolean());
    }

    public static class Bean
            implements Serializable {
        private transient AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        public Bean() {
        }

        public AtomicBoolean getAtomicBoolean() {
            return atomicBoolean;
        }
    }
}
