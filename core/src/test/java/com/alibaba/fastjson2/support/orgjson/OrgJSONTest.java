package com.alibaba.fastjson2.support.orgjson;

import com.alibaba.fastjson2.JSON;
import net.sf.json.JSONNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("util")
public class OrgJSONTest {
    @Test
    public void test() {
        JSONNull jsonNull = JSONNull.getInstance();
        assertEquals("null", JSON.toJSONString(jsonNull));
    }
}
