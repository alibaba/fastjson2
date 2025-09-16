package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderBeanTest {
    @Test
    public void testProcessObjectInputSingleItemArrayWithNullList() {
        // 测试修复后的processObjectInputSingleItemArray方法，确保不会出现空指针异常
        ObjectReader<User> objectReader = ObjectReaders.of(User.class);

        // 测试正常情况
        String json = "[{\"id\":1,\"name\":\"test\"}]";
        try (JSONReader reader = JSONReader.of(json)) {
            reader.context.config(JSONReader.Feature.SupportSmartMatch);
            User user = objectReader.readObject(reader, null, null, JSONReader.Feature.SupportSmartMatch.mask);
            assertNotNull(user);
            assertEquals(1, user.id);
            assertEquals("test", user.name);
        }

        // 测试空数组情况
        String emptyJson = "[]";
        try (JSONReader reader = JSONReader.of(emptyJson)) {
            reader.context.config(JSONReader.Feature.SupportSmartMatch);
            User user = objectReader.readObject(reader, null, null, JSONReader.Feature.SupportSmartMatch.mask);
            assertNull(user);
        }
    }

    public static class User {
        public int id;
        public String name;

        public User() {}
    }
}
