/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.ThreadingModel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.junit.jupiter.api.Assertions.*;

public class Fastjson2DatabindTest {
    private final Fastjson2Codec codec = new Fastjson2Codec();

    @Test
    public void testGenericDecoding() {
        Pojo original = new Pojo();
        original.value = "test";

        String json = codec.toString(Collections.singletonList(original), false);
        List<Pojo> correct;

        correct = codec.fromString(json, new TypeReference<List<Pojo>>() {});
        assertTrue(((List) correct).get(0) instanceof Pojo);
        assertEquals(original.value, correct.get(0).value);

        // same must apply if instead of string we use a buffer
        correct = codec.fromBuffer(Buffer.buffer(json, "UTF8"), new TypeReference<List<Pojo>>() {});
        assertTrue(((List) correct).get(0) instanceof Pojo);
        assertEquals(original.value, correct.get(0).value);

        List incorrect = codec.fromString(json, List.class);
        assertFalse(incorrect.get(0) instanceof Pojo);
        assertTrue(incorrect.get(0) instanceof Map);
        assertEquals(original.value, ((Map) (incorrect.get(0))).get("value"));
    }

    @Test
    public void testInstantDecoding() {
        Pojo original = new Pojo();
        original.instant = Instant.from(ISO_INSTANT.parse("2018-06-20T07:25:38.397Z"));
        Pojo decoded = codec.fromString("{\"instant\":\"2018-06-20T07:25:38.397Z\"}", Pojo.class);
        assertEquals(original.instant, decoded.instant);
    }

    @Test
    public void testNullInstantDecoding() {
        Pojo original = new Pojo();
        Pojo decoded = codec.fromString("{\"instant\":null}", Pojo.class);
        assertEquals(original.instant, decoded.instant);
    }

    @Test
    public void testBytesDecoding() {
        Pojo original = new Pojo();
        original.bytes = TestUtils.randomByteArray(12);
        Pojo decoded = codec.fromString("{\"bytes\":\"" + TestUtils.toBase64String(original.bytes) + "\"}", Pojo.class);
        assertArrayEquals(original.bytes, decoded.bytes);
    }

    @Test
    public void testNullBytesDecoding() {
        Pojo original = new Pojo();
        Pojo decoded = codec.fromString("{\"bytes\":null}", Pojo.class);
        assertEquals(original.bytes, decoded.bytes);
    }

    @Test
    public void testJsonArrayDeserializer() throws JsonProcessingException {
        String jsonArrayString = "[1, 2, 3]";
        JsonArray jsonArray = codec.fromString(jsonArrayString, JsonArray.class);

        assertEquals(3, jsonArray.size());
        assertEquals(new JsonArray().add(1).add(2).add(3), jsonArray);
    }

    @Test
    public void testJsonObjectDeserializer() throws JsonProcessingException {
        String jsonObjectString = "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}";

        JsonObject jsonObject = codec.fromString(jsonObjectString, JsonObject.class);

        assertEquals("value1", jsonObject.getString("key1"));
        assertEquals("value2", jsonObject.getString("key2"));
    }

    @Test
    public void testJsonObjectSerializer() throws JsonProcessingException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("key1", "value1");
        jsonObject.put("key2", "value2");
        jsonObject.put("key3", "value3");

        String jsonString = codec.toString(jsonObject, false);
        assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}", jsonString);
    }

    @Test
    public void testJsonArraySerializer() throws JsonProcessingException {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("value1");
        jsonArray.add("value2");
        jsonArray.add("value3");

        String jsonString = codec.toString(jsonArray, false);
        assertEquals("[\"value1\",\"value2\",\"value3\"]", jsonString);
    }

    @Test
    public void testInstantSerializer() throws IOException {
        Instant instant = Instant.parse("2023-06-09T12:34:56.789Z");
        String jsonString = codec.toString(instant, false);
        assertEquals("\"2023-06-09T12:34:56.789Z\"", jsonString);
    }

    @Test
    public void testInstantDeserializer() throws IOException {
        String jsonString = "\"2023-06-09T12:34:56.789Z\"";
        Instant instant = codec.fromString(jsonString, Instant.class);
        Instant expectedInstant = Instant.parse("2023-06-09T12:34:56.789Z");
        assertEquals(expectedInstant, instant);
    }

    @Test
    public void testByteArraySerializer() throws IOException {
        byte[] byteArray = "Hello, World!".getBytes();
        String jsonString = codec.toString(byteArray, false);
        String expectedBase64String = Base64.getEncoder().withoutPadding().encodeToString(byteArray);
        assertEquals("\"" + expectedBase64String + "\"", jsonString);
    }

    @Test
    public void testByteArrayDeserializer() throws IOException {
        String jsonString = "\"SGVsbG8sIFdvcmxkIQ\"";
        byte[] byteArray = codec.fromString(jsonString, byte[].class);
        byte[] expectedByteArray = Base64.getDecoder().decode("SGVsbG8sIFdvcmxkIQ");
        assertArrayEquals(expectedByteArray, byteArray);
    }

    @Test
    public void testBufferSerializer() throws IOException {
        Buffer buffer = Buffer.buffer("Hello, World!");
        String jsonString = codec.toString(buffer, false);
        assertEquals("\"SGVsbG8sIFdvcmxkIQ\"", jsonString);
    }

    @Test
    public void testBufferDeserializer() throws IOException {
        String jsonString = "\"SGVsbG8sIFdvcmxkIQ\"";
        Buffer buffer = codec.fromString(jsonString, Buffer.class);
        Buffer expectedBuffer = Buffer.buffer("Hello, World!");
        assertEquals(expectedBuffer, buffer);
    }

    private static class Pojo {
        @JSONField
        public String value;
        @JSONField
        public Instant instant;
        @JSONField
        public byte[] bytes;
    }

    @Test
    public void testPrettyPrinting() {
        JsonObject jsonObject = new JsonObject()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", "value3");

        String compact = codec.toString(jsonObject, false);
        String pretty = codec.toString(jsonObject, true);

        assertFalse(compact.equals(pretty));
        assertEquals(jsonObject, codec.fromString(pretty, JsonObject.class));
    }

    @Test
    public void testObjectMapperConfigAppliesToPrettyPrinting() throws JsonProcessingException {
        Fastjson2Codec codec = Fastjson2Factory.CODEC;
        assertNotNull(codec);
        try {
            codec.configWriterFeature(JSONWriter.Feature.WriteEnumUsingOrdinal, true);
            ThreadingModel vt = ThreadingModel.VIRTUAL_THREAD;
            String expected = String.valueOf(vt.ordinal());
            assertEquals(expected, Json.encodePrettily(vt));
            assertEquals(expected, Json.encode(vt));
        } finally {
            codec.configWriterFeature(JSONWriter.Feature.WriteEnumUsingOrdinal, false);
        }
    }
}
