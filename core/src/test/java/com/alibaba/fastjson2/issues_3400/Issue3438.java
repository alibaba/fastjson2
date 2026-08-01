package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
public class Issue3438 {
    @Test
    public void test() {
        String aaa = "{\"file\":[{\"url\":\"http\"}]}";
        Bbb bbb = JSON.parseObject(aaa, Bbb.class);
        assertEquals("[{\"url\":\"http\"}]", bbb.file);
    }

    @Data
    public static class Bbb {
        String file;
    }
}
