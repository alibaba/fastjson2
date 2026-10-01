package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConstructorDefaultsReviewTest {
    @Test
    public void absentJsonbFieldRetainsConstructorDefault() {
        Bean bean = JSONB.parseObject(JSONB.toBytes(JSONObject.of("id", 1)), Bean.class);
        assertEquals(1, bean.id);
        assertEquals("default", bean.name);
    }

    @Test
    public void explicitJsonbNullStillClearsField() {
        Bean bean = JSONB.parseObject(JSONB.toBytes(JSONObject.of("id", 1, "name", null), JSONWriter.Feature.WriteNulls), Bean.class);
        assertNull(bean.name);
    }

    public static class Bean {
        public final int id;
        public String name = "default";

        @JSONCreator
        public Bean(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
