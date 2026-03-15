package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64URLSafeStringAsByteArrayTest {
    private final byte[][] testData = new byte[][]{
            // 触发 '_' 字符解码
            new byte[]{-1, -1, -1},

            // 触发 '-' 字符解码
            new byte[]{-5, -17, -66},

            // 对应无 '=' 填充的场景 (原需 2 个填充)
            new byte[]{-1, -1, -1, -1},

            // 对应无 '=' 填充的场景 (原需 1 个填充)
            new byte[]{-1, -1, -1, -1, -1},

            // 边界测试：空数组
            new byte[0]
    };

    private String getUrlSafeUnpaddedJson() {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < testData.length; i++) {
            sb.append("\"").append(encoder.encodeToString(testData[i])).append("\"");
            if (i < testData.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Test
    public void testReadBase64URLSafeStringAsByteArray() {
        String json = getUrlSafeUnpaddedJson();
        byte[][] parsed = JSON.parseObject(json, byte[][].class, JSONReader.Feature.Base64URLSafeStringAsByteArray);
        assertEquals(testData.length, parsed.length);
        for (int i = 0; i < testData.length; i++) {
            assertArrayEquals(testData[i], parsed[i]);
        }
    }
}
