package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1828 {
    private static final int SIZE = 50 * 1024 * 1024;

    @Test
    public void test() {
        DTO dto = new DTO();
        dto.setVedioBytes(new byte[SIZE]);
        String json = JSON.toJSONString(dto, JSONWriter.Feature.LargeObject);
        assertEquals(SIZE, JSON.parseObject(json, DTO.class).getVedioBytes().length);
    }

    @Data
    public class DTO {
        private byte[] vedioBytes;
    }
}
