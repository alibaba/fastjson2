package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AsmReaderReviewTest {
    @Test
    public void binarySmartMatchUsesFoldedFieldHash() {
        ObjectReader<UppercaseField> objectReader = ObjectReaderCreatorASM.INSTANCE.createObjectReader(UppercaseField.class);
        assertTrue(objectReader.getClass().getSimpleName().startsWith("ORG_"));
        byte[] bytes = JSONB.toBytes(JSONObject.of("x", 123));
        try (JSONReader reader = JSONReader.ofJSONB(bytes, JSONReader.Feature.SupportSmartMatch)) {
            assertEquals(123, objectReader.readJSONBObject(reader, UppercaseField.class, null, 0).x);
        }
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(JSONObject.of("X", 456)))) {
            assertEquals(456, objectReader.readJSONBObject(reader, UppercaseField.class, null, 0).x);
        }
    }

    @Test
    public void functionReaderRetainsFeatures() {
        ObjectReader<Value> objectReader = ObjectReaderCreatorASM.INSTANCE.createObjectReader(
                null, null, null, JSONReader.Feature.SupportSmartMatch.mask, null, Value::new, null,
                ObjectReaders.fieldReaderString("value", (Value bean, String value) -> bean.value = value));
        try (JSONReader reader = JSONReader.of("{\"VALUE\":\"ok\"}")) {
            assertEquals("ok", objectReader.readObject(reader, null, null, 0).value);
        }
    }

    @Test
    public void functionReaderRetainsRootName() {
        ObjectReader<Value> objectReader = ObjectReaderCreatorASM.INSTANCE.createObjectReader(
                null, null, "root", 0, null, Value::new, null,
                ObjectReaders.fieldReaderString("value", (Value bean, String value) -> bean.value = value));
        try (JSONReader reader = JSONReader.of("{\"root\":{\"value\":\"ok\"}}")) {
            assertEquals("ok", objectReader.readObject(reader, null, null, 0).value);
        }
    }

    @Test
    public void constructorNullOnErrorDoesNotGenerateInvalidBytecode() {
        ObjectReader<ConstructorValue> objectReader = ObjectReaderCreatorASM.INSTANCE.createObjectReader(ConstructorValue.class);
        try (JSONReader reader = JSONReader.of("{\"value\":123}")) {
            assertEquals(123, objectReader.readObject(reader, ConstructorValue.class, null, 0).value);
        }
    }

    public static class ConstructorValue {
        public final Integer value;

        @JSONCreator
        public ConstructorValue(@JSONField(name = "value", deserializeFeatures = JSONReader.Feature.NullOnError) Integer value) {
            this.value = value;
        }
    }

    public static class UppercaseField {
        @JSONField(name = "X")
        public int x;
    }

    public static class Value {
        public String value;
    }
}
