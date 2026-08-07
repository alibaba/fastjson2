package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.json.bvtVO.basic.LongPrimitiveEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2344 {
    @Test
    public void test_for_issue() throws Exception {
        LongPrimitiveEntity vo = new LongPrimitiveEntity(9007199254741992L);

        assertEquals("{\"value\":\"9007199254741992\"}",
                JSON.toJSONString(vo, SerializerFeature.BrowserCompatible));
    }
}
