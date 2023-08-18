package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1754 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isRequired", true);

        String json = jsonObject.toJSONString();
        assertTrue(
                JSON.parseObject(
                        json,
                        AttributeEntity.class,
                        JSONReader.Feature.SupportSmartMatch
                )
                        .isRequired
        );

        AttributeEntity attribute = jsonObject.to(
                AttributeEntity.class,
                JSONReader.Feature.SupportSmartMatch
        );
        assertTrue(attribute.isRequired);
    }

    public static class AttributeEntity {
        private boolean isRequired;

        public boolean isRequired() {
            return isRequired;
        }

        public void setRequired(boolean required) {
            isRequired = required;
        }
    }
}
