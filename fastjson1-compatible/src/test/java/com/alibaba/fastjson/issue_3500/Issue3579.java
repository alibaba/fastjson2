package com.alibaba.fastjson.issue_3500;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3579 {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals("1",
                JSON.toJSONString(new BigDecimal("1"))
        );

        assertEquals("1",
                JSON.toJSONString(new BigDecimal("1"), SerializerFeature.WriteClassName)
        );
    }
}
