package com.alibaba.fastjson2.issues_1900;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    /**
     * 测试 hutool JSONObject
     * error result {"config":{"ignoreNullValue":true,"ignoreError":false,"ignoreCase":false,"transientSupport":true,"checkDuplicate":false,"stripTrailingZeros":true},"raw":{"id":123}}
     */
    @Test
    void testHutoolJSONObject() {
        JSONObject hutool = new JSONObject();
        hutool.put("id", 123);
        JSONObject deserializedHutool = JSON.parseObject(
                JSON.toJSONString(hutool, JSONWriter.Feature.FieldBased),
                JSONObject.class
        );
        assertEquals(hutool, deserializedHutool);
    }

    /**
     * 在 dubbo 的序列化配置下 测试 hutool JSONObject
     */
    @Test
    void testHutoolJSOBObjectWithDubboConf() {
        //writeObject
        JSONObject hutool = new JSONObject();
        hutool.put("id", 123);
        byte[] bytes = JSONB.toBytes(
                hutool,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
// readObject
        Object result = JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.autoTypeFilter(true, JSONObject.class),
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        assertEquals(hutool, result);
    }
}
