package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1948 {
    @Test
    public void test() {
        A a = new A();
        a.list = Lists.newLinkedList();
        a.list.add("test 1");
        a.list.add("test 2");

        assertEquals("{\"list\":\"TEST 1\"\"TEST 2\"}", JSON.toJSONString(a));
    }

    public static class A {
        @JSONField(serializeUsing = FieldWriteTest.class)
        public List<String> list;
    }

    public static class FieldWriteTest
            implements ObjectWriter<List<String>> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            for (String s : (List<String>) object) {
                jsonWriter.writeString(s.toUpperCase());
            }
        }
    }
}
