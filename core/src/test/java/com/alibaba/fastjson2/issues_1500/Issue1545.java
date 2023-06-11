package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue1545 {
    @Test
    public void testString() {
        String s = "{\n" +
                "    \"fieldPermissions\": [\n" +
                "        {\n" +
                "            \"mode\": \"READ\",\n" +
                "            \"fieldId\": \"updatedTime\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        Exception ex = null;
        try {
            JSONObject.parseObject(s, ApproveActionConfig.class);
        } catch (Exception e) {
            ex = e;
        }
        assertTrue(ex instanceof JSONException);
        assertEquals("ObjectReaderImplEnum parses error, JSONReader not forward when field type belongs to collection to avoid OOM", ex.getMessage());
    }

    @Data
    public class ApproveActionConfig {
        private List<PermMode> fieldPermissions;
    }

    public enum PermMode {
        READ,

        WRITE,

        NONE
    }
}
