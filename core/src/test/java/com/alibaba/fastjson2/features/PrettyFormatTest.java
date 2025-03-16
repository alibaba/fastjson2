package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrettyFormatTest {
    @Test
    public void testArray() {
        JSONArray array = JSONArray.of(1, 2, 3);
        assertEquals("[1,2,3]", array.toString());
        assertEquals("[1,2,3]", array.toString(OptimizedForAscii));

        assertEquals(
                "[\n" +
                        "\t1,\n" +
                        "\t2,\n" +
                        "\t3\n" +
                        "]",
                array
                        .toString(PrettyFormat));
        assertEquals(
                "[\n" +
                        "\t1,\n" +
                        "\t2,\n" +
                        "\t3\n" +
                        "]",
                array
                        .toString(PrettyFormat, OptimizedForAscii));
    }

    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("id", 123, "value", "abc");

        assertEquals("{\"id\":123,\"value\":\"abc\"}", jsonObject.toString());
        assertEquals("{\"id\":123,\"value\":\"abc\"}", jsonObject.toString(OptimizedForAscii));

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith4Space, OptimizedForAscii));

        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith2Space));
        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                jsonObject
                        .toString(PrettyFormatWith2Space, OptimizedForAscii));
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.setId(123);
        bean.setValue("abc");

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space, OptimizedForAscii));

        Bean1[] array = new Bean1[] {bean};
        assertEquals(
                "[\n" +
                        "\t{\n" +
                        "\t\t\"id\":123,\n" +
                        "\t\t\"value\":\"abc\"\n" +
                        "\t}\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormat));
        assertEquals(
                "[\n" +
                        "    {\n" +
                        "        \"id\":123,\n" +
                        "        \"value\":\"abc\"\n" +
                        "    }\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormatWith4Space));
    }

    @Data
    private static class Bean1 {
        private int id;
        private String value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.setId(123);
        bean.setValue("abc");

        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormatWith4Space, OptimizedForAscii));

        Bean2[] array = new Bean2[] {bean};
        assertEquals(
                "[\n" +
                        "\t{\n" +
                        "\t\t\"id\":123,\n" +
                        "\t\t\"value\":\"abc\"\n" +
                        "\t}\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormat));
        assertEquals(
                "[\n" +
                        "    {\n" +
                        "        \"id\":123,\n" +
                        "        \"value\":\"abc\"\n" +
                        "    }\n" +
                        "]",
                JSON.toJSONString(array, PrettyFormatWith4Space));
    }

    @Data
    public static class Bean2 {
        private int id;
        private String value;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.values.add("a01");
        bean.values.add("a02");

        assertEquals(
                "{\n" +
                        "\t\"values\":[\n" +
                        "\t\t\"a01\",\n" +
                        "\t\t\"a02\"\n" +
                        "\t]\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"values\":[\n" +
                        "\t\t\"a01\",\n" +
                        "\t\t\"a02\"\n" +
                        "\t]\n" +
                        "}",
                JSON.toJSONString(bean, PrettyFormat, OptimizedForAscii));
    }

    public static class Bean3 {
        public List<String> values = new ArrayList<>();
    }

    @Test
    public void ofPrettyUTF16() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter.startObject();
        jsonWriter.writeNameValue("id", 123);
        jsonWriter.endObject();
        jsonWriter.close();

        assertEquals("{\n" +
                "\t\"id\"123\n" +
                "}", jsonWriter.toString());

        jsonWriter.incrementIndent();
        assertEquals(1, jsonWriter.level());
        jsonWriter.println();
        assertEquals("{\n" +
                "\t\"id\"123\n" +
                "}\n\t", jsonWriter.toString());
        jsonWriter.decrementIdent();
        assertEquals(0, jsonWriter.level());
    }

    @Test
    public void ofPrettyUTF8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter = JSONWriter.ofPretty(jsonWriter);
        jsonWriter.startObject();
        jsonWriter.writeNameValue("id", 123);
        jsonWriter.endObject();
        jsonWriter.close();

        assertEquals("{\n" +
                "\t\"id\"123\n" +
                "}", jsonWriter.toString());

        jsonWriter.incrementIndent();
        assertEquals(1, jsonWriter.level());
        jsonWriter.println();
        assertEquals("{\n" +
                "\t\"id\"123\n" +
                "}\n\t", jsonWriter.toString());
        jsonWriter.decrementIdent();
        assertEquals(0, jsonWriter.level());
    }

    @Test
    public void testJSONObject() {
        {
            JSONObject object = JSONObject.of(
                    "f0", null,
                    "f1", 101,
                    "f2", 102L,
                    "f3", new BigDecimal("103"),
                    "f4", new JSONObject(),
                    "f5", new JSONArray(),
                    "f6", (short) 106,
                    "f7", true
            );
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of(PrettyFormat, WriteNulls);
                jsonWriter.write(object);
                assertEquals("{\n" +
                        "\t\"f0\":null,\n" +
                        "\t\"f1\":101,\n" +
                        "\t\"f2\":102,\n" +
                        "\t\"f3\":103,\n" +
                        "\t\"f4\":{},\n" +
                        "\t\"f5\":[],\n" +
                        "\t\"f6\":106,\n" +
                        "\t\"f7\":true\n" +
                        "}", jsonWriter.toString());
            }
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(PrettyFormat);
            jsonWriter.write(JSONObject.of());
            assertEquals("{}", jsonWriter.toString());
        }
    }
}
