package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ValidateResult;
import org.junit.jupiter.api.Test;

public class Issue684 {
    @Test
    public void test() {
        String schemaDef = "{\n" +
                "                    type: \"object\",\n" +
                "                    properties: {\n" +
                "                        longitude: {\n" +
                "                            type: \"number\",\n" +
                "                            minimum: -180,\n" +
                "                            maximum: 180\n" +
                "                        },\n" +
                "                        latitude: {\n" +
                "                            type: \"number\",\n" +
                "                            minimum: -90,\n" +
                "                            maximum: 90\n" +
                "                        }\n" +
                "                    },\n" +
                "                    required: [\"longitude\", \"latitude\"]\n" +
                "                }";

        JSONSchema schema = JSONSchema.parseSchema(schemaDef);
        ValidateResult result = schema.validate(
                JSONObject.of("longitude", 201, "latitude1", 30.2741)
        );
        System.out.println(result.getMessage());
    }
}
