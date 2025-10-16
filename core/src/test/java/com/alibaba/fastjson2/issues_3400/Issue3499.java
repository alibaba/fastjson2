package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class Issue3499 {
    @Test
    public void test() {
        Message<String> obj = new Message<>("Hello");
        byte[] bytes = JSONB.toBytes(obj, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(true, obj.getClass());
        Object result = JSONB.parseObject(bytes, obj.getClass(), handler,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Message<T>
            implements Serializable {
        T msg;
    }

    @Setter
    @Getter
    public static class ParentTestOrder
            implements Serializable {
        private static final long serialVersionUID = 721230004160956721L;
        private boolean flag;
    }

    @Setter
    @Getter
    public static class TestOrder extends ParentTestOrder {
        private String name;
    }

    @Test
    public void test1() {
        TestOrder order = new TestOrder();
        order.setName("test");
        order.setFlag(true);

        byte[] bytes1 = JSONB.toBytes(
                order,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        JSONB.parseObject(
                bytes1,
                TestOrder.class,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
    }
}
