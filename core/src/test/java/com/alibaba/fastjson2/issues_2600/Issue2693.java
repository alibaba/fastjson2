package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

/**
 * @author 张治保
 * @since 2024/6/12
 */
public class Issue2693 {
    @Test
    @SneakyThrows
    void test() {
        String json = "{\"@type\":\"com.alibaba.fastjson2.issues_2600.Issue2693$FormInstance\",\"id\":\"jdjdjksjkjskddd111\",\"widgets\":{\"@type\":\"java.util.HashMap\",\"_S_SERIAL\":\"LYBD-20240611-001\"}}";
        FormInstance formInstance = JSON.parseObject(json, FormInstance.class, Feature.SupportAutoType);
        JSONAssert.assertEquals(
                JSON.toJSONString(formInstance, SerializerFeature.WriteClassName), json, true
        );
    }

    @Getter
    @Setter
    private static class FormInstance{
        private String id;
        private Map<String, Object> widgets;
    }
}
