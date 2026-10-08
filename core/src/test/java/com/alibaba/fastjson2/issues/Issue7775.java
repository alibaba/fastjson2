package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7775 {
    @Test
    public void extractRootArray() {
        String json = "[{\"nationTwoAbbr\":\"CN\"},{\"nationTwoAbbr\":\"IN\"}]";
        assertEquals(JSONArray.of("CN", "IN"), JSONPath.extract(json, "$.nationTwoAbbr"));
        assertExtract(json, JSONArray.of("CN", "IN"));
        assertExtract(json, JSONArray.of("CN", "IN"), JSONPath.Feature.AlwaysReturnList);
    }

    @Test
    public void extractSingleElementArray() {
        String json = "[{\"nationTwoAbbr\":\"CN\"}]";
        assertExtract(json, JSONArray.of("CN"));
        assertExtract(json, JSONArray.of("CN"), JSONPath.Feature.AlwaysReturnList);
    }

    @Test
    public void extractArrayWithoutValues() {
        for (String json : new String[]{"[]", "[{}]", "[{\"nationTwoAbbr\":null}]"}) {
            assertExtract(json, null);
            assertExtract(json, new JSONArray(), JSONPath.Feature.AlwaysReturnList);
        }
    }

    @Test
    public void extractMixedArray() {
        String json = "[null,{},{\"nationTwoAbbr\":null},{\"nationTwoAbbr\":\"CN\"}]";
        assertExtract(json, JSONArray.of("CN"));
    }

    @Test
    public void extractArrayValues() {
        String json = "[{\"nationTwoAbbr\":[\"CN\",\"IN\"]}]";
        assertExtract(json, JSONArray.of("CN", "IN"));
        assertExtract(json, JSONArray.of("CN", "IN"), JSONPath.Feature.AlwaysReturnList);

        assertExtract("[{\"nationTwoAbbr\":[\"CN\"]},{\"nationTwoAbbr\":[\"IN\"]}]", JSONArray.of("CN", "IN"));
    }

    @Test
    public void extractObject() {
        assertExtract("{\"other\":1,\"nationTwoAbbr\":\"CN\"}", "CN");
        assertExtract("{\"nationTwoAbbr\":[\"CN\",\"IN\"]}", JSONArray.of("CN", "IN"));
    }

    private void assertExtract(String json, Object expected, JSONPath.Feature... features) {
        JSONPath path = JSONPath.of("$.nationTwoAbbr", features);
        assertEquals(expected, path.extract(json));
        assertEquals(expected, path.extract(json.getBytes(StandardCharsets.UTF_8)));
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(JSON.parse(json)))) {
            assertEquals(expected, path.extract(reader));
        }
    }
}
