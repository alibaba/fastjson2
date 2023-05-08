package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1450 {
    public interface Bean<K extends Serializable>
            extends Serializable {
        K getId();
    }

    public interface Member
            extends Bean<Long> {
    }

    @Getter
    @Setter
    public static class AbstractEntity
            implements Bean<Long> {
        protected Long id;
    }

    @Getter
    @Setter
    public static class AbstractMember
            extends AbstractEntity
            implements Member {
        protected String name;
    }

    @Getter
    @Setter
    public static class Student
            extends AbstractMember {
        Integer age;
    }

    @Test
    public void test_json() {
        final Student obj = new Student();
        obj.setId(1L);
        obj.setAge(18);
        obj.setName("Jim");

        byte[] bytes = JSON.toJSONBytes(obj, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        assertNotNull(bytes);

        Object result = JSON.parseObject(bytes, Member.class, (Filter) null,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        assertNotNull(result);
        assertInstanceOf(Student.class, result);

        Student stu = (Student) result;
        assertEquals(1L, stu.getId());
        assertEquals(18, stu.getAge());
        assertEquals("Jim", stu.getName());
    }

    @Test
    public void test_jsonb() {
        final Student obj = new Student();
        obj.setId(1L);
        obj.setAge(18);
        obj.setName("Jim");

        byte[] bytes = JSONB.toBytes(obj, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        assertNotNull(bytes);

        Object result = JSONB.parseObject(bytes, Member.class, (Filter) null,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        assertNotNull(result);
        assertInstanceOf(Student.class, result);

        Student stu = (Student) result;
        assertEquals(1L, stu.getId());
        assertEquals(18, stu.getAge());
        assertEquals("Jim", stu.getName());
    }
}
