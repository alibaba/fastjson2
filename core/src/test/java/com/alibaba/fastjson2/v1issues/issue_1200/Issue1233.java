package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 30/05/2017.
 */
public class Issue1233 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{\"type\":\"floorV2\",\"templateId\":\"x123\"}");

        JSON.mixIn(Area.class, AreaMixIn.class);
        JSON.mixIn(FloorV2.class, FloorV2MixIn.class);

        FloorV2 floorV2 = (FloorV2) jsonObject.toJavaObject(Area.class);
        assertNotNull(floorV2);
        assertEquals("x123", floorV2.templateId);
    }

    @JSONType(seeAlso = FloorV2.class, typeKey = "type")
    public interface AreaMixIn {
    }

    @JSONType(typeName = "floorV2")
    public interface FloorV2MixIn {
    }

    public interface Area {
        public static final String TYPE_SECTION = "section";
        public static final String TYPE_FLOORV1 = "floorV1";
        public static final String TYPE_FLOORV2 = "floorV2";

        String getName();
    }

    public static class Section
            implements Area {
        public List<Area> children;

        public String type;

        public String templateId;

        public String getName() {
            return templateId;
        }
    }

    public static class FloorV1
            implements Area {
        public String type;
        public String templateId;

        public String getName() {
            return templateId;
        }
    }

    public static class FloorV2
            implements Area {
        public List<Area> children;

        public String type;

        public String templateId;

        public String getName() {
            return templateId;
        }
    }
}
