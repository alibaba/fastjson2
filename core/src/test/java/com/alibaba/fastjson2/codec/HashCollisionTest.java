package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashCollisionTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.courseId = "123";
        bean.userId = "456";
        bean.sourceId = "789";

        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);

        Bean bean1 = JSON.parseObject(str, Bean.class, JSONReader.Feature.FieldBased);
        assertEquals(bean1.courseId, bean.courseId);
        assertEquals(bean1.userId, bean.userId);
        assertEquals(bean1.sourceId, bean.sourceId);

        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.WriteNameAsSymbol);

        Bean bean2 = JSONB.parseObject(jsonb, Bean.class, JSONReader.Feature.FieldBased);
        assertEquals(bean2.courseId, bean.courseId);
        assertEquals(bean2.userId, bean.userId);
        assertEquals(bean2.sourceId, bean.sourceId);
    }

    @Data
    public static class Bean {
        private String courseId;
        private String userId;
        private String sourceId;
    }
}
