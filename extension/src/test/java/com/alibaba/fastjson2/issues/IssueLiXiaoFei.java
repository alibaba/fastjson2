package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IssueLiXiaoFei {
    @Test
    public void test0() {
        String str = "[{\"@type\":\"org.springframework.security.core.authority.SimpleGrantedAuthority\",\"authority\":\"ADMIN\"}]";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Object.class, JSONReader.Feature.ErrorOnNotSupportAutoType));

        JSONArray parsed = (JSONArray) JSON.parseObject(str, Object.class, JSONReader.autoTypeFilter("org.springframework.security.core."), JSONReader.Feature.ErrorOnNotSupportAutoType);
        assertNotNull(parsed);
        assertEquals(org.springframework.security.core.authority.SimpleGrantedAuthority.class, parsed.get(0).getClass());
    }
}
