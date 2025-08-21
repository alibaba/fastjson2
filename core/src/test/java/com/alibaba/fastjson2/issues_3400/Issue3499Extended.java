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

import static org.junit.jupiter.api.Assertions.*;

public class Issue3499Extended {
    @Test
    public void testGenericMessageWithString() {
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

        assertEquals(obj.getMsg(), ((Message<String>) result).getMsg());
    }

    @Test
    public void testGenericMessageWithInteger() {
        Message<Integer> obj = new Message<>(42);
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

        assertEquals(obj.getMsg(), ((Message<Integer>) result).getMsg());
    }

    @Test
    public void testGenericMessageWithCustomObject() {
        Message<Person> obj = new Message<>(new Person("John", 30));
        byte[] bytes = JSONB.toBytes(obj, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(true, obj.getClass(), Person.class);
        Object result = JSONB.parseObject(bytes, obj.getClass(), handler,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        Person originalPerson = obj.getMsg();
        Person resultPerson = ((Message<Person>) result).getMsg();
        assertEquals(originalPerson.getName(), resultPerson.getName());
        assertEquals(originalPerson.getAge(), resultPerson.getAge());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Message<T>
            implements Serializable {
        T msg;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Person
            implements Serializable {
        String name;
        int age;
    }
}
