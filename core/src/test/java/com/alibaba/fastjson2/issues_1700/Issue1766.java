package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.ArraySchema;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.schema.ObjectSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1766 {
    @Test
    public void test() {
        String str = "{\n" +
                "  \"$defs\": {\n" +
                "    \"person\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"name\": { \"type\": \"string\" },\n" +
                "        \"children\": { \"$ref\": \"#/$defs/personList\" }\n" +
                "      }\n" +
                "    },\n" +
                "    \"personList\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": { \"$ref\": \"#/$defs/person\" }\n" +
                "    }\n" +
                "  },\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"family\": { \"$ref\": \"#/$defs/person\" }\n" +
                "  }\n" +
                "}";

        JSONObject object = JSON.parseObject(str);
        ObjectSchema schema = (ObjectSchema) JSONSchema.of(object);
        JSONSchema person = schema.getDefs("person");
        ArraySchema personList = (ArraySchema) schema.getDefs("personList");
        assertSame(person, personList.getItemSchema());

        assertTrue(
                schema.validate(
                        JSON.parseObject("\n" +
                                "{\"family\":{\"name\":\"1\",\"children\":[{\"name\":\"2\"}]}}")
                ).isSuccess()
        );
    }
}
