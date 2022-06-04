package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue338 {
    @Test
    public void test() {
        JSONObject parse = (JSONObject) JSON.parse("{\"@type\":\"com.af.v4.system.api.model.LoginUser\",\"expireTime\":1653496818784,\"ipaddr\":\"127.0.0.1\",\"loginTime\":1653453618784,\"sysUser\":{\"@type\":\"com.af.v4.system.api.domain.SysUser\",\"password\":\"1\",\"userId\":3810,\"userName\":\"zkk\"},\"token\":\"e3322327-9a35-4715-a7b5-387b20530c0e\",\"userid\":3810,\"username\":\"zkk\"}");
        assertEquals("com.af.v4.system.api.model.LoginUser", parse.get("@type"));
    }
}
