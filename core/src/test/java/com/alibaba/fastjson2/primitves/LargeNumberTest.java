package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LargeNumberTest {
    @Test
    public void test_0() {
        String str = "{\"val\":0.784018486000000000000000000000000000000}";
        JSONObject object = JSON.parseObject(str);
        assertEquals(new BigDecimal("0.103453752158123073073250785136463577088"), object.get("val"));
    }
}
