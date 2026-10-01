package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BeanInfoRequiredTest {
    @Test
    public void requiredPropertyExtendsSchemaWithoutRequiredArray() {
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.schema = "{\"minProperties\":1}";
        beanInfo.required("id");
        beanInfo.required("name");
        JSONObject schema = JSON.parseObject(beanInfo.schema);
        assertEquals(1, schema.getIntValue("minProperties"));
        assertEquals(JSONArray.of("id", "name"), schema.getJSONArray("required"));
    }

    @Test
    public void requiredAnnotationCombinesWithTypeSchema() {
        assertEquals("value", JSON.parseObject("{\"name\":\"value\"}", RequiredBean.class).name);
        assertThrows(JSONSchemaValidException.class, () -> JSON.parseObject("{}", RequiredBean.class));
    }

    @JSONType(schema = "{\"minProperties\":1}")
    public static class RequiredBean {
        @JSONField(required = true)
        public String name;
    }
}
