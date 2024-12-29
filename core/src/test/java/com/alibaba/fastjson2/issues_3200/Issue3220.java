package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3220 {
    @Test
    public void test() {
        Param param = new Param();
        param.setUserName("张三");
        param.setCode("001");
        param.setSort("1");
        param.setStatus("正常");
        assertEquals("{\"code\":\"001\",\"sort\":\"1\",\"status\":\"正常\",\"userName\":\"张三\"}", JSON.toJSONString(param));
    }

    @Data
    private static class Param {
        public String UserName;
        String Code;
        String Sort;
        String Status;
    }
}
