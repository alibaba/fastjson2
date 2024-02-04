package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author 张治保
 * @since 2024/2/4
 */
public class Issue2233 {
    @Test
    void test() throws JSONException {
        String json = "{\"log_entries\": null, \"logEntries2\": [],\"map\": null}";
        Obj obj = assertDoesNotThrow(
                () -> JSON.parseObject(json, Obj.class)
        );
        assertNotNull(obj);
        JSONAssert.assertEquals(json, JSON.toJSONString(obj, JSONWriter.Feature.WriteNulls), true);
    }

    @Getter
    @Setter
    static class Obj{
        @JsonProperty("log_entries")
        private List logEntries;
        private List<Object> logEntries2;
        private Map map;
    }
}
