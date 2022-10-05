package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue698 {
    @Test
    public void testDeserialize() {
        if (TestUtils.GRAALVM) {
            return;
        }

        String jsonString = "{\"middleEntity\":{\"entity\":{\"name\":\"jhahah\"}}}";
        BigEntity result = JSON.parseObject(jsonString, BigEntity.class);
        assertEquals(
                "jhahah",
                result
                        .getMiddleEntity()
                        .getEntity()
                        .getName()
        );
    }

    @Test
    public void testDeserialize1() {
        if (TestUtils.GRAALVM) {
            return;
        }

        String jsonString = "{\"middleEntity\":{\"entity\":{\"name\":\"jhahah\"}}}";
        JSONObject jsonObject = JSON.parseObject(jsonString);
        byte[] jsonbBytes = jsonObject.toJSONBBytes();

        BigEntity result = JSONB.parseObject(jsonbBytes, BigEntity.class);
        assertEquals(
                "jhahah",
                result
                        .getMiddleEntity()
                        .getEntity()
                        .getName()
        );
    }

    static interface Entity {
        String getName();
    }

    static interface MiddleEntity {
        Entity getEntity();
    }

    static class BigEntity {
        private MiddleEntity middleEntity;

        public BigEntity() {}

        public void setMiddleEntity(MiddleEntity middleEntity) {
            this.middleEntity = middleEntity;
        }

        public MiddleEntity getMiddleEntity() {
            return middleEntity;
        }
    }
}
