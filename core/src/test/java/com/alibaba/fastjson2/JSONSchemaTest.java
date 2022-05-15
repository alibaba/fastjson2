package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JSONSchemaTest {
    @Test
    public void test0() {
        URL url = JSONSchemaTest.class.getClassLoader().getResource("schema/schema_01.json");
        JSONObject object = JSON.parseObject(url, JSONObject.class);

        JSONSchema.ObjectSchema schema = (JSONSchema.ObjectSchema) JSONSchema.of(object);
        JSONSchema.ObjectSchema schema1 = (JSONSchema.ObjectSchema) JSON.parseObject(url, JSONSchema::of);
        assertEquals(schema.hashCode(), schema1.hashCode());
        assertEquals(schema, schema1);

        assertEquals("Product", schema.getTitle());
        assertEquals("A product from Acme's catalog", schema.getDescription());
        assertEquals(JSONSchema.Type.Object, schema.getType());

        assertEquals(3, schema.getProperties().size());

        JSONSchema propertyId = schema.getProperty("id");
        assertEquals("The unique identifier for a product", propertyId.getDescription());
        assertEquals(JSONSchema.Type.Integer, propertyId.getType());

        JSONSchema propertyName = schema.getProperty("name");
        assertEquals("Name of the product", propertyName.getDescription());
        assertEquals(JSONSchema.Type.String, propertyName.getType());

        JSONSchema propertyPrice = schema.getProperty("price");
        assertNull(propertyPrice.getDescription());
        assertEquals(JSONSchema.Type.Number, propertyPrice.getType());

        Set<String> required = schema.getRequired();
        assertEquals(3, required.size());
        assertTrue(required.contains("id"));
        assertTrue(required.contains("name"));
        assertTrue(required.contains("price"));

        schema.validate(JSONObject
                .of(
                        "id", 1,
                        "name", "",
                        "price", 0
                )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                            "id", 1,
                            "name", ""
                        )
            )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", "1",
                                "name", "",
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", 1,
                                "name", 1,
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", 1,
                                "name", "",
                                "price", "x"
                        )
                )
        );
    }
}
