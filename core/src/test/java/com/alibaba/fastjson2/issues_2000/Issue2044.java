package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2044 {
    JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName
    };

    JSONReader.Feature[] features = {
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.UseNativeObject
    };

    @Test
    public void test() {
        User1 user1 = new User1();
        user1.name = "abc";
        byte[] bytes = JSONB.toBytes(user1, writerFeatures);
        User0 user0 = JSONB.parseObject(bytes, User0.class, JSONReader.Feature.FieldBased);
        assertEquals(user0.name, user1.name);
    }

    @Test
    public void test1() {
        User0 user1 = new User0();
        user1.name = "abc";
        byte[] bytes = JSONB.toBytes(user1, writerFeatures);
        User1 user0 = JSONB.parseObject(bytes, User1.class, JSONReader.Feature.FieldBased);
        assertEquals(user0.name, user1.name);
    }

    public class User0
            implements Serializable {
        private static final long serialVersionUID = 8295348195732708179L;
        private Long userId;  // 新增
        private String name;
    }

    public class User1
            implements Serializable {
        private static final long serialVersionUID = 8295348195732708179L;
        private String name;

    }
}
