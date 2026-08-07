package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue605 {
    @Test
    public void test() {
        WinningInformationBo bo = new WinningInformationBo();
        bo.isSuccess = true;
        bo.wxId = "abc";
        bo.parems = new HashMap<>();

        String str = JSON.toJSONString(bo);
        assertEquals("{\"parems\":{},\"success\":true,\"wxId\":\"abc\"}", str);

        WinningInformationBo bo1 = JSON.parseObject(str, WinningInformationBo.class);
        assertEquals(bo.wxId, bo1.wxId);
        assertEquals(bo.isSuccess, bo1.isSuccess);
        assertEquals(bo.parems, bo1.parems);
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class WinningInformationBo{
        private boolean isSuccess;
        private String wxId;
        private Map<String, String> parems;
    }
}
