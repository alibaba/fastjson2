package com.alibaba.fastjson.util;

import org.junit.jupiter.api.Test;

public class IOUtilsTest {
    @Test
    public void close() {
        IOUtils.close(null);
        IOUtils.close(() -> {
            throw new RuntimeException();
        });
        IOUtils.close(() -> {});
    }
}
