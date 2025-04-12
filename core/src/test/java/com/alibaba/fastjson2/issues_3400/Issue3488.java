package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3488 {
    @Test
    public void test() {
        String str = "{\"val\":0.06451612903225806}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.UseBigDecimalForDoubles);
        Object val = object.get("val");
        assertEquals(BigDecimal.class, val.getClass());
    }
}
