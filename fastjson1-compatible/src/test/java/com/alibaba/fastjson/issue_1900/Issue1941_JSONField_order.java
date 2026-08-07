package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1941_JSONField_order {
    @Test
    public void test_for_issue() {
        String json = "{\"type\":\"floorV2\",\"templateId\":\"x123\",\"name\":\"floorname2\"}";
        FloorV2 a = (FloorV2) JSON.parseObject(json, Area.class);
        assertEquals("floorname2", a.name);
        assertEquals("x123", a.templateId);
    }

    @JSONType(seeAlso = {FloorV2.class}, typeKey = "type")
    public static interface Area {
    }

    @JSONType(typeName = "floorV2")
    public static class FloorV2
            implements Area {
        @JSONField(ordinal = -1)
        public String type;
        public String templateId;
        public String name;
    }
}
