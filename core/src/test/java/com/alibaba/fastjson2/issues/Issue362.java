package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue362 {
    @Test
    public void test() {
        String emtpy = "";
        assertNull(JSON.parseObject(emtpy, User.class));

        byte[] emptyBytes = emtpy.getBytes(StandardCharsets.UTF_8);
        assertNull(JSON.parseObject(emptyBytes, User.class));
        assertNull(JSON.parseObject(emptyBytes, 0, emptyBytes.length, StandardCharsets.US_ASCII, User.class));

        String text = "{\"username\":\"dfs\",\"name\":\"xxx\",\"exception\":\"\"}";
        User user = JSON.parseObject(text, User.class);
        assertNull(user.exception);

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        user = JSON.parseObject(bytes, User.class);
        assertNull(user.exception);

        user = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII, User.class);
        assertNull(user.exception);

        JSONReader strReader = TestUtils.createJSONReaderStr(text);
        user = strReader.read(User.class);
        assertNull(user.exception);
    }

    @Data
    public class User{
        private BusinessException exception;
    }

    public class BusinessException
            extends RuntimeException{
    }
}
