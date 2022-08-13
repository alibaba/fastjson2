package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue596 {
    @Test
    public void test() {
        String str3 = "{\"wx_id\":\"xxx\",\"success\":true,\"params\":null}";
        Bean bean = JSON.parseObject(str3, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("xxx", bean.wxId);
        assertEquals(true, bean.isSuccess);
        assertEquals(null, bean.params);
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Bean {
        private boolean isSuccess;
        private String wxId;
        private Map<String, String> params;
    }
}
