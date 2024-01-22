package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author 张治保
 * @since 2024/1/19
 */
public class Issue2183 {
    @Test
    void test() throws JSONException {
        Map data = new HashMap();
        data.put("now", LocalDateTime.now());
        data.put(LocalDateTime.now(), System.currentTimeMillis());
        //默认 LocalDateTime序列化为字符串 所以开启BrowserCompatible和不开启的结果应该一致
        final String json1 = JSONObject.toJSONString(data, JSONWriter.Feature.BrowserCompatible);
        final String json2 = JSONObject.toJSONString(data);
        JSONAssert.assertEquals(json1, json2, true);
    }

    @Test
    void testBrowserCompatible() throws JSONException {
        Map data = new HashMap();
        data.put(true, true);
        data.put(null, true);
        data.put(1L, 1L);
        data.put(Long.MIN_VALUE, "min");
        data.put(Long.MAX_VALUE, "max");
        String json = JSONObject.toJSONString(data, JSONWriter.Feature.BrowserCompatible);
        JSONAssert.assertEquals("{\"true\":true,\"null\":true,\"1\":1,\"-9223372036854775808\":\"min\",\"9223372036854775807\":\"max\"}", json, true);
    }

    /**
     * 引用检测 测试
     * todo: 未通过 ❌ 应该使用兼容性更好的 $['key'] 👀 而不是 $.key 作为引用路径
     */
    @Test
    void testReferenceDetection() {
        //不支持 ❌ ref -> $.2024-01-22 16:39:40.328
        String json = "{\"2024-01-22 16:39:40.328\":{\"name\":\"jsonObject1\"},\"{\\\"name\\\":\\\"key\\\"}\":{\"$ref\":\"$.2024-01-22 16:39:40.328\"}}";
        JSONObject jsonObject1 = JSON.parseObject(json);
        //支持 👍 ref -> $['2024-01-22 16:39:40.328']
        json = "{\"2024-01-22 16:39:40.328\":{\"name\":\"jsonObject2\"},\"{\\\"name\\\":\\\"key\\\"}\":{\"$ref\":\"$['2024-01-22 16:39:40.328']\"}}";
        JSONObject jsonObject2 = JSON.parseObject(json);
        //不支持 ❌ ref -> $.{\"name\":\"key\"}"}
        json = "{\"{\\\"name\\\":\\\"key\\\"}\":{\"name\":\"jsonObject3\"},\"2024-01-22 16:39:40.328\":{\"$ref\":\"$.{\\\"name\\\":\\\"key\\\"}\"}}";
        JSONObject jsonObject3 = JSON.parseObject(json);
        //支 持 👍  ref -> $['{\"name\":\"key\"}']
        json = "{\"{\\\"name\\\":\\\"key\\\"}\":{\"name\":\"jsonObject4\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$['{\\\"name\\\":\\\"key\\\"}']\"}}";
        JSONObject jsonObject4 = JSON.parseObject(json);
        //支持 👍 ref -> $.null
        json = "{\"null\":{\"name\":\"jsonObject5\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$.null\"}}";
        JSONObject jsonObject5 = JSON.parseObject(json);
        //支持 👍 ref -> $['null']
        json = "{\"null\":{\"name\":\"jsonObject6\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$['null']\"}}";
        JSONObject jsonObject6 = JSON.parseObject(json);
        //不支持 ❌ ref -> $.1
        json = "{1:{\"name\":\"jsonObject7\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$.1\"}}";
        JSONObject jsonObject7 = JSON.parseObject(json, JSONReader.Feature.AllowUnQuotedFieldNames);
        //支持 👍 ref -> $[1]
        json = "{1:{\"name\":\"jsonObject8\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$[1]\"}}";
        JSONObject jsonObject8 = JSON.parseObject(json, JSONReader.Feature.AllowUnQuotedFieldNames);
        //支持 👍 ref -> $.1
        json = "{\"1\":{\"name\":\"jsonObject9\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$.1\"}}";
        JSONObject jsonObject9 = JSON.parseObject(json);
        //支持 👍 ref -> $[1]
        json = "{\"1\":{\"name\":\"jsonObject10\"},\"2024-01-22 15:50:24.525\":{\"$ref\":\"$[1]\"}}";
        JSONObject jsonObject10 = JSON.parseObject(json);
    }

    @Test
    void testObjectKey() throws JSONException {
        Map data = new HashMap();
        data.put(
                new TestObject().setName("name1"),
                new TestObject().setName("name2")
        );
        String json = JSONObject.toJSONString(data, JSONWriter.Feature.BrowserCompatible);
        JSONAssert.assertEquals(
                "{\"{\\\"name\\\":\\\"name1\\\"}\":{\"name\":\"name2\"}}",
                json,
                true
        );
        data.clear();
        data.put(
                new ArrayList<Object>() {
                    {
                        add(1);
                        add(false);
                        add(new TestObject().setName("array"));
                    }
                },
                Boolean.FALSE
        );
        json = JSONObject.toJSONString(data, JSONWriter.Feature.BrowserCompatible);
        JSONAssert.assertEquals(
                "{\"[1,false,{\\\"name\\\":\\\"array\\\"}]\":false}",
                json,
                true
        );
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @Accessors(chain = true)
    public static class TestObject {
        private String name;
    }
}
