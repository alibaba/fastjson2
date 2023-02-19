package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.schema.model.Person;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONSchemaTest4 {
    @Test
    public void max() {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of(
                        "properties",
                        JSONObject.of(
                                "age",
                                JSONObject.of("maximum", 120)
                        )
                )
        );

        ValidateResult result = schema.validate(JSONObject.of("age", 121));
        assertTrue(result.getMessage().contains("age"));
    }

    @Test
    public void min() {
        JSONSchema schema = JSONSchema.of(
                JSONObject.of(
                        "properties",
                        JSONObject.of(
                                "age",
                                JSONObject.of("minimum", 0)
                        )
                )
        );

        ValidateResult result = schema.validate(JSONObject.of("age", -10));
        assertTrue(result.getMessage().contains("age"));
    }

    @Test
    public void testValidateRequired() throws IOException {
        final File file = new File("src/test/resources/data/person.json");
        final String personSchema = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        final JSONSchema jsonSchema = JSONSchema.parseSchema(personSchema);

        // create the person without amount property which is required in the person schema.
        final Person person = new Person();
        person.setId(1);
        person.setAge(22);

        final ValidateResult result = jsonSchema.validate(person);
        assertFalse(result.isSuccess());
        assertFalse(result.getMessage().isEmpty());
        assertTrue(result.getMessage().contains("amount"));
    }
}
