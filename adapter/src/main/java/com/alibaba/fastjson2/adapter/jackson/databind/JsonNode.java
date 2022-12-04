package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.*;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeSerializer;
import com.alibaba.fastjson2.adapter.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class JsonNode
        implements TreeNode, Iterable<JsonNode> {
    @Override
    public JsonNode get(String fieldName) {
        return null;
    }

    public abstract String asText();

    public int asInt() {
        return asInt(0);
    }

    public boolean asBoolean() {
        return false;
    }

    public int asInt(int defaultValue) {
        return defaultValue;
    }

    public long asLong() {
        return asLong(0L);
    }

    public long asLong(long defaultValue) {
        return defaultValue;
    }

    @Override
    public String toString() {
        return asText();
    }

    public String toPrettyString() {
        return JSON.toJSONString(JSONWriter.Feature.PrettyFormat);
    }

    public JsonNode path(String fieldName) {
        return null;
    }

    public boolean isArray() {
        return false;
    }

    public Iterator<Map.Entry<String, JsonNode>> fields() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<JsonNode> iterator() {
        throw new UnsupportedOperationException();
    }

    public Iterator<JsonNode> elements() {
        return Collections.emptyIterator();
    }

    public byte[] binaryValue() throws IOException {
        return null;
    }

    public String textValue() {
        return null;
    }

    public boolean isTextual() {
        return false;
    }

    public boolean has(int index) {
        return get(index) != null;
    }

    @Override
    public JsonNode get(int index) {
        return null;
    }

    public boolean has(String fieldName) {
        return get(fieldName) != null;
    }

    public JsonParser.NumberType numberType() {
        return null;
    }

    public <T extends JsonNode> T deepCopy() {
        // TODO deepCopy
        throw new JSONException("TODO");
    }

    public JsonNodeType getNodeType() {
        throw new JSONException("TODO");
    }

    public JsonNode path(int index) {
        throw new UnsupportedOperationException();
    }

    public JsonParser traverse() {
        throw new UnsupportedOperationException();
    }

    public JsonParser traverse(ObjectCodec codec) {
        throw new UnsupportedOperationException();
    }

    protected JsonNode _at(JsonPointer ptr) {
        throw new UnsupportedOperationException();
    }

    public JsonNode findValue(String fieldName) {
        throw new UnsupportedOperationException();
    }

    public JsonNode findPath(String fieldName) {
        throw new UnsupportedOperationException();
    }

    public JsonNode findParent(String fieldName) {
        throw new UnsupportedOperationException();
    }

    public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
        throw new UnsupportedOperationException();
    }

    public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
        throw new UnsupportedOperationException();
    }

    public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
        throw new UnsupportedOperationException();
    }

    public void serialize(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean canConvertToLong() {
        return false;
    }

    public JsonToken asToken() {
        throw new UnsupportedOperationException();
    }

    public long longValue() {
        return 0L;
    }

    public int intValue() {
        return 0;
    }

    public boolean booleanValue() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public BigDecimal decimalValue() {
        return BigDecimal.ZERO;
    }

    public float floatValue() {
        return 0;
    }

    public float doubleValue() {
        return 0;
    }

    public boolean canConvertToInt() {
        return false;
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public final boolean isValueNode() {
        switch (getNodeType()) {
            case ARRAY:
            case OBJECT:
            case MISSING:
                return false;
            default:
                return true;
        }
    }

    public final boolean isNull() {
        return getNodeType() == JsonNodeType.NULL;
    }

    public JsonNode required(String propertyName) throws IllegalArgumentException {
        return _reportRequiredViolation("Node of type `%s` has no fields", getClass().getName());
    }

    protected <T> T _reportRequiredViolation(String msgTemplate, Object... args) {
        throw new IllegalArgumentException(String.format(msgTemplate, args));
    }

    public boolean isMissingNode() {
        return false;
    }

    public final boolean isBoolean() {
        return getNodeType() == JsonNodeType.BOOLEAN;
    }

    public boolean hasNonNull(String fieldName) {
        JsonNode n = get(fieldName);
        return (n != null) && !n.isNull();
    }

    public final JsonNode at(String jsonPtrExpr) {
        return at(JsonPointer.compile(jsonPtrExpr));
    }

    public final JsonNode at(JsonPointer ptr) {
        throw new JSONException("TODO");
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isBigDecimal() {
        return false;
    }

    public boolean isBigInteger() {
        return false;
    }

    public final boolean isContainerNode() {
        final JsonNodeType type = getNodeType();
        return type == JsonNodeType.OBJECT || type == JsonNodeType.ARRAY;
    }

    public double asDouble() {
        return 0;
    }

    public BigInteger bigIntegerValue() {
        return BigInteger.ZERO;
    }
}
