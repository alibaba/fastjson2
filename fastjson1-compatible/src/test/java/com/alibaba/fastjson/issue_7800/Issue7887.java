package com.alibaba.fastjson.issue_7800;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue7887 {
    static Stream<Arguments> parsers() {
        return Stream.of(
                Arguments.of("string", (Function<String, Object>) JSON::parse),
                Arguments.of("features", (Function<String, Object>) text -> JSON.parse(text, new Feature[0])),
                Arguments.of("config", (Function<String, Object>) text -> JSON.parse(text, new ParserConfig())),
                Arguments.of("configFeatures", (Function<String, Object>) text -> JSON.parse(text, new ParserConfig(), new Feature[0])),
                Arguments.of("configMask", (Function<String, Object>) text -> JSON.parse(text, new ParserConfig(), JSON.DEFAULT_PARSER_FEATURE)),
                Arguments.of("mask", (Function<String, Object>) text -> JSON.parse(text, JSON.DEFAULT_PARSER_FEATURE)),
                Arguments.of("bytes", (Function<String, Object>) text -> JSON.parse(text.getBytes(StandardCharsets.UTF_8), new Feature[0]))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parsers")
    void sharedArrayValue(String name, Function<String, Object> parser) {
        List<Integer> shared = Arrays.asList(1, 2);
        JSONObject firstObject = new JSONObject();
        firstObject.put("x", shared);
        JSONObject secondObject = new JSONObject();
        secondObject.put("x", shared);
        JSONArray source = new JSONArray();
        source.add(firstObject);
        source.add(secondObject);
        JSONArray value = (JSONArray) parser.apply(JSON.toJSONString(source));
        Object first = value.getJSONObject(0).get("x");
        assertNotNull(first);
        assertSame(first, value.getJSONObject(1).get("x"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parsers")
    void sharedObjectValue(String name, Function<String, Object> parser) {
        JSONObject value = (JSONObject) parser.apply("{\"first\":{\"id\":1},\"second\":{\"$ref\":\"$.first\"}}");
        assertNotNull(value.get("first"));
        assertSame(value.get("first"), value.get("second"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parsers")
    void ordinaryValues(String name, Function<String, Object> parser) {
        JSONObject value = (JSONObject) parser.apply("{\"id\":1,\"items\":[2,3]}");
        assertEquals(1, value.getIntValue("id"));
        assertEquals(2, value.getJSONArray("items").size());
        assertNull(parser.apply("null"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parsers")
    void rejectsTrailingInput(String name, Function<String, Object> parser) {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> parser.apply("{} false"));
    }
}
