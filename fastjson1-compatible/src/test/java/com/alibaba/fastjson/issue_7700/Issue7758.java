package com.alibaba.fastjson.issue_7700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7758 {
    public static class Session {
        private String id;
        private String host;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    private static final Type OBJECT_TYPE = Object.class;

    private static String payload() {
        Session s = new Session();
        s.setId("session-001");
        s.setHost("127.0.0.1");
        return JSON.toJSONString(s, SerializerFeature.WriteClassName);
    }

    private static ParserConfig autoTypeConfig() {
        ParserConfig config = new ParserConfig();
        config.setAutoTypeSupport(true);
        return config;
    }

    private static void assertSession(Object o, String entryPoint) {
        assertNotNull(o, entryPoint + " returned null");
        assertTrue(o instanceof Session, entryPoint + " expected Session but got " + o.getClass());
        assertEquals("session-001", ((Session) o).getId(), entryPoint + " id");
        assertEquals("127.0.0.1", ((Session) o).getHost(), entryPoint + " host");
    }

    private static void assertJsonObject(Object o, String entryPoint) {
        assertTrue(o instanceof JSONObject, entryPoint + " expected JSONObject but got " + o.getClass());
    }

    @Test
    public void testAutoTypeSupportViaParse() {
        String json = payload();
        assertSession(JSON.parse(json, autoTypeConfig()), "JSON.parse(String, ParserConfig)");
    }

    @Test
    public void testDefaultIgnoresAutoType() {
        String json = payload();
        // Without autoTypeSupport, @type must be ignored and a plain JSONObject returned.
        assertJsonObject(JSON.parse(json, new ParserConfig()), "JSON.parse(String, ParserConfig)");
    }

    @Test
    public void testPerCallIgnoreAutoTypeWinsOverAutoTypeSupport() {
        String json = payload();
        // The per-call Feature.IgnoreAutoType must suppress @type resolution even though the
        // ParserConfig enables autoTypeSupport.
        assertJsonObject(
                JSON.parse(json, autoTypeConfig(), Feature.IgnoreAutoType),
                "JSON.parse(String, ParserConfig, Feature.IgnoreAutoType)"
        );
    }

    @Test
    public void testAutoTypeSupportViaParseObject() {
        String json = payload();
        // Object.class forces @type resolution instead of parsing into a statically known type,
        // and keeps expectClass compatible so checkAutoType does not reject with "type not match".
        assertSession(JSON.parseObject(json, OBJECT_TYPE, autoTypeConfig()),
                "JSON.parseObject(String, Type, ParserConfig)");
    }

    @Test
    public void testPerCallSupportAutoTypeStillWins() {
        String json = payload();
        // The pre-existing v1 Feature.SupportAutoType -> JSONReader.Feature.SupportAutoType
        // mapping must keep working on its own, independently of the config flag.
        assertSession(JSON.parse(json, new ParserConfig(), Feature.SupportAutoType),
                "JSON.parse(String, ParserConfig, Feature.SupportAutoType)");
    }

    /**
     * Every read entry point that accepts a {@code ParserConfig} must honour
     * {@code setAutoTypeSupport(true)}. Reverting any single routed call site turns one row red
     * instead of leaving the whole suite green.
     */
    @Test
    public void testAutoTypeSupportOnEveryRoutedEntryPoint() throws Exception {
        String json = payload();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        assertSession(
                JSON.parse(json, autoTypeConfig()),
                "JSON.parse(String, ParserConfig)");

        assertSession(
                JSON.parse(json, autoTypeConfig(), Feature.OrderedField),
                "JSON.parse(String, ParserConfig, Feature...)");

        assertSession(
                JSON.parse(json, autoTypeConfig(), 0),
                "JSON.parse(String, ParserConfig, int)");

        assertSession(
                JSON.parseObject(json, OBJECT_TYPE, autoTypeConfig(), Feature.OrderedField),
                "JSON.parseObject(String, Type, ParserConfig, Feature...)");

        assertSession(
                JSON.parseObject(json, OBJECT_TYPE, autoTypeConfig(), null, 0),
                "JSON.parseObject(String, Type, ParserConfig, ParseProcess, int, Feature...)");

        assertSession(
                JSON.parseObject(bytes, StandardCharsets.UTF_8, OBJECT_TYPE, autoTypeConfig(), null, 0),
                "JSON.parseObject(byte[], Charset, Type, ParserConfig, ParseProcess, int, Feature...)");

        assertSession(
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, OBJECT_TYPE,
                        autoTypeConfig(), null, 0),
                "JSON.parseObject(byte[], int, int, Charset, Type, ParserConfig, ParseProcess, int, Feature...)");

        assertSession(
                JSON.parseObject(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8, OBJECT_TYPE,
                        autoTypeConfig(), null, 0),
                "JSON.parseObject(InputStream, Charset, Type, ParserConfig, ParseProcess, int, Feature...)");

        // A target class equal to the written @type is not discriminating, so read the array
        // generically and inspect the element type instead.
        List<Object> list = JSON.parseArray("[" + json + "]", Object.class, autoTypeConfig());
        assertNotNull(list, "JSON.parseArray(String, Class, ParserConfig) returned null");
        assertEquals(1, list.size(), "JSON.parseArray(String, Class, ParserConfig) size");
        assertSession(list.get(0), "JSON.parseArray(String, Class, ParserConfig)");

        // fastjson 1.x honoured the flag on both of these idioms as well.
        try (DefaultJSONParser parser = new DefaultJSONParser(json, autoTypeConfig())) {
            assertSession(parser.parse(), "new DefaultJSONParser(String, ParserConfig).parse()");
        }

        assertSession(
                JSONPath.read(json, "$", OBJECT_TYPE, autoTypeConfig()),
                "JSONPath.read(String, String, Type, ParserConfig)");
    }

    /**
     * The negative direction for the two paths added on top of the {@code JSON} statics: without
     * the flag they must stay inert, so the change is a gate and not an unconditional switch.
     * The container type is intentionally not asserted here: {@code DefaultJSONParser.parse()}
     * returns fastjson2's {@link JSONObject} rather than the compatible one.
     */
    @Test
    public void testDefaultJsonParserAndJsonPathStayInertWithoutAutoType() {
        String json = payload();

        try (DefaultJSONParser parser = new DefaultJSONParser(json, new ParserConfig())) {
            Object o = parser.parse();
            assertFalse(o instanceof Session,
                    "new DefaultJSONParser(String, ParserConfig).parse() resolved @type: " + o.getClass());
        }

        Object pathResult = JSONPath.read(json, "$", OBJECT_TYPE, new ParserConfig());
        assertFalse(pathResult instanceof Session,
                "JSONPath.read(String, String, Type, ParserConfig) resolved @type: " + pathResult.getClass());
    }
}
