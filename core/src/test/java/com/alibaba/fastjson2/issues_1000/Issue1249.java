package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1249 {
    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        map.put("phoneNum", "1");
        map.put("varDefine", "{\"success\":\"yes\"}");
        map.put("tagSession", "[{\"tagId\":\"6908494829\",\"tagName\":\"不是本人\"}]");
        CallInfo callInfo = JSON.parseObject(JSON.toJSONString(map), CallInfo.class);
        assertNotNull(callInfo);
    }

    @Data
    public static class CallInfo {
        private String phoneNum;

        @JSONField(deserializeUsing = JsonDeserializer.class)
        private Map<String, Object> varDefine;

        @JSONField(deserializeUsing = TagDeserializer.class)
        private List<Tag> tagSession;
    }

    @Data
    public static class Tag {
        private String tagId;
        private String tagName;
    }

    public static class JsonDeserializer
            implements ObjectReader<Map<String, Object>> {
        @Override
        public Map<String, Object> readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            String value = jsonReader.readString();
            return JSONObject.parseObject(value);
        }
    }

    public static class TagDeserializer
            implements ObjectReader<List<Tag>> {
        @Override
        public List<Tag> readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            String value = jsonReader.readString();
            return JSON.parseArray(value, Tag.class);
        }
    }
}
