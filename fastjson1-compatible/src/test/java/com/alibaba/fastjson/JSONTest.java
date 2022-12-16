package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.*;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTest {
    @Test
    public void test() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertEquals(HashMap.class, JSON.parseObject(bytes).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(bytes, Feature.OrderedField).getInnerMap().getClass());
    }

    @Test
    public void test1() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        assertEquals(HashMap.class, JSON.parseObject(str).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(str, Feature.OrderedField).getInnerMap().getClass());
    }

    @Test
    public void testNull() throws Exception {
        assertNull(JSON.parseObject((InputStream) null, Object.class));
        assertNull(JSON.parseObject((InputStream) null, (Type) Object.class));
    }

    @Test
    public void testInputStream() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        Bean bean = JSON.parseObject(new ByteArrayInputStream(utf8), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    @Test
    public void testCharArray() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        Bean bean = JSON.parseObject(str.toCharArray(), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    @Test
    public void toJSONString() {
        Bean bean = new Bean();
        bean.id = 123;
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(bean, null, null, new SerializeFilter[0])
        );
    }

    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void toJSONString1() {
        BeanAware bean = new BeanAware(123);
        assertEquals("123", JSON.toJSONString(bean));

        {
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(BeanAware.class);
            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, null, null, null, 0);
            assertEquals("null", jsonWriter.toString());
        }

        {
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(BeanAware.class);
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            objectWriter.writeJSONB(jsonWriter, null, null, null, 0);
            byte[] bytes = jsonWriter.getBytes();
            assertNull(JSONB.parse(bytes));
        }
    }

    @Test
    public void parseArray() {
        JSONArray jsonArray = JSON.parseArray("[1,2,3]");
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parseArray1() {
        List<Long> jsonArray = JSON.parseArray("[1,2,3]", Long.class, ParserConfig.global);
        assertNotNull(jsonArray);
        assertEquals(3, jsonArray.size());
    }

    @Test
    public void parse() {
        JSONArray jsonArray = (JSONArray) JSON.parse("[1,2,3]");
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parse1() {
        JSONArray jsonArray = (JSONArray) JSON.parse("[1,2,3]", Feature.ErrorOnNotSupportAutoType);
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parse2() {
        byte[] bytes = "[1,2,3]".getBytes(StandardCharsets.UTF_8);
        JSONArray jsonArray = (JSONArray) JSON.parse(bytes);
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parse3() {
        byte[] bytes = "[1,2,3]".getBytes(StandardCharsets.UTF_8);
        JSONArray jsonArray = (JSONArray) JSON.parse(bytes, Feature.ErrorOnNotSupportAutoType);
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parse4() {
        assertNull(JSON.parse(null, ParserConfig.global, Feature.ErrorOnNotSupportAutoType));
        assertNull(JSON.parse("", ParserConfig.global, Feature.ErrorOnNotSupportAutoType));

        String str = "[1,2,3]";
        JSONArray jsonArray = (JSONArray) JSON.parse(str, ParserConfig.global, Feature.ErrorOnNotSupportAutoType);
        assertEquals("[1,2,3]", jsonArray.toJSONString());
    }

    @Test
    public void parse5() {
        assertNull(JSON.parse(null, ParserConfig.global));
        assertNull(JSON.parse("", ParserConfig.global));

        String str = "[1,2,3]";
        JSONArray jsonArray = (JSONArray) JSON.parse(str, ParserConfig.global);
        assertEquals("[1,2,3]", jsonArray.toJSONString());

        JSONArray jsonArray2 = (JSONArray) JSON.parse(str, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE);
        assertEquals("[1,2,3]", jsonArray2.toJSONString());
    }

    @Test
    public void toJSONBytes() {
        assertEquals(
                "null",
                new String(
                        JSON.toJSONBytes(
                                null,
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                Collections.emptyList(),
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );

        assertEquals(
                "null",
                new String(
                        JSON.toJSONBytes(
                                null,
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                null,
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                Collections.emptyList(),
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                null,
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );

        assertEquals(
                "null",
                new String(
                        JSON.toJSONBytes(
                                null,
                                SerializeConfig.global,
                                (SerializeFilter) null,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                Collections.emptyList(),
                                SerializeConfig.global,
                                (SerializeFilter) null,
                                SerializerFeature.BrowserSecure)
                )
        );

        assertEquals(
                "null",
                new String(
                        JSON.toJSONBytes(
                                null,
                                SerializeConfig.global,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                Collections.emptyList(),
                                SerializeConfig.global,
                                SerializerFeature.BrowserSecure)
                )
        );

        assertEquals(
                "null",
                new String(
                        JSON.toJSONBytes(
                                null,
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                Collections.emptyList(),
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
    }

    @Test
    public void toJSONBytes1() {
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                StandardCharsets.UTF_8,
                                Collections.emptyList(),
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                (String) null,
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                StandardCharsets.UTF_8,
                                Collections.emptyList(),
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                "",
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
        assertEquals(
                "[]",
                new String(
                        JSON.toJSONBytes(
                                StandardCharsets.UTF_8,
                                Collections.emptyList(),
                                SerializeConfig.global,
                                new SerializeFilter[0],
                                JSON.DEFFAULT_DATE_FORMAT,
                                JSON.DEFAULT_GENERATE_FEATURE,
                                SerializerFeature.BrowserSecure)
                )
        );
    }

    @Test
    public void writeJSONString() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSON.writeJSONString(
                os,
                StandardCharsets.UTF_8,
                Collections.emptyList(),
                SerializerFeature.BrowserSecure
        );

        assertEquals(
                "[]",
                os.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    public void writeJSONString1() throws Exception {
        StringWriter os = new StringWriter();
        JSON.writeJSONString(
                os,
                Collections.emptyList(),
                SerializerFeature.BrowserSecure
        );

        assertEquals(
                "[]",
                os.toString()
        );
    }

    @Test
    public void writeJSONString2() throws Exception {
        StringWriter os = new StringWriter();
        JSON.writeJSONString(
                os,
                Collections.emptyList(),
                JSON.DEFAULT_GENERATE_FEATURE,
                SerializerFeature.BrowserSecure
        );

        assertEquals(
                "[]",
                os.toString()
        );
    }

    @Test
    public void writeJSONString3() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JSON.writeJSONString(
                os,
                Collections.emptyList(),
                JSON.DEFAULT_GENERATE_FEATURE,
                SerializerFeature.BrowserSecure
        );

        assertEquals(
                "[]",
                os.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    public void writeJSONString4() throws Exception {
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JSON.writeJSONString(
                    os,
                    StandardCharsets.UTF_8,
                    Collections.emptyList(),
                    SerializeConfig.global,
                    new SerializeFilter[0],
                    "",
                    JSON.DEFAULT_GENERATE_FEATURE,
                    SerializerFeature.BrowserSecure
            );

            assertEquals(
                    "[]",
                    os.toString(StandardCharsets.UTF_8)
            );
        }
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JSON.writeJSONString(
                    os,
                    StandardCharsets.UTF_8,
                    Collections.emptyList(),
                    SerializeConfig.global,
                    new SerializeFilter[0],
                    null,
                    JSON.DEFAULT_GENERATE_FEATURE,
                    SerializerFeature.BrowserSecure
            );

            assertEquals(
                    "[]",
                    os.toString(StandardCharsets.UTF_8)
            );
        }
    }

    @Test
    public void parseObject() {
        byte[] bytes = "{}".getBytes();

        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        bytes,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        bytes,
                        0,
                        bytes.length,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );

        assertNull(
                JSON.parseObject(
                        (byte[]) null,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        new byte[0],
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );

        assertNull(
                JSON.parseObject(
                        (byte[]) null,
                        0,
                        0,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        new byte[0],
                        0,
                        0,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global, (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void parseObject1() {
        char[] chars = "{}".toCharArray();
        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        chars,
                        chars.length,
                        HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );

        assertNull(
                JSON.parseObject(
                        (char[]) null,
                        0,
                        HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        new char[0],
                        0,
                        HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void parseObject2() {
        String str = "{}";
        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        str,
                        HashMap.class,
                        ParserConfig.global,
                        null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );

        assertNull(
                JSON.parseObject(
                        (String) null,
                        HashMap.class,
                        ParserConfig.global,
                        null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        HashMap.class,
                        ParserConfig.global,
                        null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void parseObject3() {
        String str = "{}";
        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        str,
                        HashMap.class,
                        (ParseProcess) null,
                        Feature.ErrorOnNotSupportAutoType
                )
        );

        assertNull(
                JSON.parseObject(
                        (String) null,
                        HashMap.class,
                        (ParseProcess) null,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        HashMap.class,
                        (ParseProcess) null,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        null,
                        HashMap.class
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        HashMap.class
                )
        );
        assertNull(
                JSON.parseObject(
                        (String) null,
                        HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        (String) null,
                        (Type) HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        (Type) HashMap.class,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        (String) null,
                        (Type) HashMap.class,
                        ParserConfig.global,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        "",
                        (Type) HashMap.class,
                        ParserConfig.global,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void parseObject4() throws Exception {
        byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
        {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            assertEquals(
                    new HashMap<>(),
                    JSON.parseObject(
                            is,
                            StandardCharsets.UTF_8,
                            HashMap.class,
                            ParserConfig.global,
                            (ParseProcess) null,
                            JSON.DEFAULT_PARSER_FEATURE,
                            Feature.ErrorOnNotSupportAutoType
                    )
            );
        }

        {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            assertEquals(
                    new HashMap<>(),
                    JSON.parseObject(
                            is,
                            StandardCharsets.UTF_8,
                            HashMap.class,
                            (ParserConfig) null,
                            (ParseProcess) null,
                            JSON.DEFAULT_PARSER_FEATURE,
                            Feature.ErrorOnNotSupportAutoType
                    )
            );
        }

        assertNull(
                JSON.parseObject(
                        (InputStream) null,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        (ParserConfig) null,
                        (ParseProcess) null,
                        JSON.DEFAULT_PARSER_FEATURE,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
        assertNull(
                JSON.parseObject(
                        (InputStream) null,
                        StandardCharsets.UTF_8,
                        HashMap.class,
                        ParserConfig.global,
                        Feature.AllowArbitraryCommas
                )
        );
    }

    @Test
    public void parseObject5() {
        String str = "{}";
        assertEquals(
                new HashMap<>(),
                JSON.parseObject(
                        str,
                        new TypeReference<HashMap<String, Integer>>() {
                        }.getType(),
                        0,
                        Feature.ErrorOnNotSupportAutoType
                )
        );
    }

    @Test
    public void toJSON() {
        assertEquals("123", JSON.toJSON("123", SerializeConfig.global));
    }

    @Test
    public void writeJSONString5() {
        JSONArray array = new JSONArray();
        StringBuilder buf = new StringBuilder();
        array.writeJSONString(buf);
        assertEquals("[]", buf.toString());
    }

    public static class BeanAware
            implements JSONAware {
        private int id;

        public BeanAware(int id) {
            this.id = id;
        }

        @Override
        public String toJSONString() {
            return Integer.toString(id);
        }
    }

    @Test
    public void isProxy() {
        assertFalse(TypeUtils.isProxy(Object.class));
    }

    @Test
    public void test4() {
        JSON.addMixInAnnotations(Bean4.class, Bean4Mixin.class);
        assertNotNull(JSON.getMixInAnnotations(Bean4.class));
        JSON.removeMixInAnnotations(Bean4.class);
        JSON.clearMixInAnnotations();
    }

    static class Bean4 {
    }

    static class Bean4Mixin {
    }

    @Test
    public void parseArray2() {
        assertThrows(JSONException.class, () -> JSON.parseArray("[", Long.class));
    }

    @Test
    public void test5() {
        SerializeConfig config = new SerializeConfig(true);
        config.put(Bean5.class, new ObjectSerializer() {
            @Override
            public void write(
                    JSONSerializer serializer,
                    Object object,
                    Object fieldName,
                    Type fieldType,
                    int features
            ) throws IOException {
                Bean5 bean = (Bean5) object;
                if (bean == null) {
                    serializer.writeNull();
                    return;
                }

                JSONObject jsonObject = new JSONObject().fluentPut("id", bean.id);
                serializer.write(jsonObject.toString());
            }
        });

        Bean5 bean = new Bean5();
        bean.id = 101;
        bean.name = "DataWorks";
        assertEquals("\"{\\\"id\\\":101}\"", JSON.toJSONString(bean, config));
    }

    public static class Bean5 {
        public int id;
        public String name;
    }
}
