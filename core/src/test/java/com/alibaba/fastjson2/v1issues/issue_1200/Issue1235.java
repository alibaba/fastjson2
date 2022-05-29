package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 30/05/2017.
 */
public class Issue1235 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"type\":\"floorV2\",\"templateId\":\"x123\"}";

        FloorV2 floorV2 = (FloorV2) JSON.parseObject(json, Area.class);
        assertNotNull(floorV2);
        assertNotNull(floorV2.templateId);
        assertEquals("x123", floorV2.templateId);
        assertEquals("floorV2", floorV2.type);

        String json2 = JSON.toJSONString(floorV2, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"type\":\"floorV2\",\"templateId\":\"x123\"}", json2);
    }

    @JSONType(seeAlso = FloorV2.class, typeKey = "type")
    public interface Area {
        public static final String TYPE_SECTION = "section";
        public static final String TYPE_FLOORV1 = "floorV1";
        public static final String TYPE_FLOORV2 = "floorV2";
    }

    @JSONType(typeName = "floorV2")
    public static class FloorV2
            implements Area {
        @JSONField(ordinal = 1)
        public String type;

        @JSONField(ordinal = 2)
        public String templateId;
    }
}
