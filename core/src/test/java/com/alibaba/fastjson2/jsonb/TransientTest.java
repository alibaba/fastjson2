package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter.Feature;
import com.alibaba.fastjson2.util.UnsafeUtils;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        Bean target = (Bean)JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );

        assertNull(target.getAtomicBoolean());

        Bean target2 = (Bean)JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseDefaultConstructorAsPossible
        );
        assertNotNull(target2.getAtomicBoolean());
    }


    public static class Bean implements Serializable {
        private transient AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        public Bean() {

        }

        public AtomicBoolean getAtomicBoolean() {
            return atomicBoolean;
        }
    }
}
