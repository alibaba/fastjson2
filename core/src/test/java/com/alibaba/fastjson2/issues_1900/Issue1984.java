package com.alibaba.fastjson2.issues_1900;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Issue1984 {
    @Test
    public void test() {
        BizDataDTO bo = new BizDataDTO();
        bo.setPageJson(new JSONArray());

        byte[] bytes = JSONB.toBytes(bo, JSONWriter.Feature.FieldBased);
        BizDataDTO bo1 = JSONB.parseObject(bytes, BizDataDTO.class, JSONReader.Feature.FieldBased);
        assertArrayEquals(bytes, JSONB.toBytes(bo1));
    }

    @Test
    public void test1() {
        JSONArray array = new JSONArray();
        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.FieldBased);
        JSONArray array1 = JSONB.parseObject(bytes, JSONArray.class, JSONReader.Feature.FieldBased);
        assertArrayEquals(bytes, JSONB.toBytes(array1));
    }

    @Test
    public void test2() {
        JSONObject array = new JSONObject();
        byte[] bytes = JSONB.toBytes(array, JSONWriter.Feature.FieldBased);
        JSONObject array1 = JSONB.parseObject(bytes, JSONObject.class, JSONReader.Feature.FieldBased);
        assertArrayEquals(bytes, JSONB.toBytes(array1));
    }

    @Data
    public static class BizDataDTO
            implements Serializable {
        /**
         * 页面结构
         */
        private JSONArray pageJson;
    }
}
