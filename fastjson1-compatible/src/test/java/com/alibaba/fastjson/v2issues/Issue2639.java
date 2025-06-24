package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2639 {
    @Test
    public void dynPageToJSONString() {
        DynPage data = JSON.parseObject(json, DynPage.class);
        assertNotNull(data);

        String jsonv = JSON.toJSONString(data);
        assertNotNull(jsonv);
    }

    @Data
    public static class DynPage {
        private List<DynArea> dets;
    }

    @Data
    public static class DynArea {
        private List<DynField> fields;
    }

    @Data
    public static class DynField {
        private String fname;
        private Serializable value;
    }

    static final String json = "{\n" +
            "  \"dets\": [\n" +
            "    {\n" +
            "      \"fields\": [\n" +
            "        {\n" +
            "          \"fname\": \"字段A\",\n" +
            "          \"value\": \"\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"fname\": \"字段B\",\n" +
            "          \"value\": \"\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"fields\": [\n" +
            "        {\n" +
            "          \"fname\": \"字段C\",\n" +
            "          \"value\": \"\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"fname\": \"字段D\",\n" +
            "          \"value\": \"\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
