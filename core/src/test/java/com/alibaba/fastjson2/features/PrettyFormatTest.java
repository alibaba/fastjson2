package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrettyFormatTest {
    @Test
    public void test() {
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
                        .toString(PrettyFormat));
        assertEquals(
                "{\n" +
                        "\t\"id\":123,\n" +
                        "\t\"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
                        .toString(PrettyFormat, OptimizedForAscii));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
                        .toString(PrettyFormatWith4Space));
        assertEquals(
                "{\n" +
                        "    \"id\":123,\n" +
                        "    \"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
                        .toString(PrettyFormatWith4Space, OptimizedForAscii));

        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
                        .toString(PrettyFormatWith2Space));
        assertEquals(
                "{\n" +
                        "  \"id\":123,\n" +
                        "  \"value\":\"abc\"\n" +
                        "}",
                JSONObject.of("id", 123, "value", "abc")
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
}
