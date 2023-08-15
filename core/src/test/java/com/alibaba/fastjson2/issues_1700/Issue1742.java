package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1742 {
    @Getter
    public static class TestFastJson {
        private long a = 1443605444282772302L;
        private List<Long> b = new ArrayList<Long>() {{
                add(1443605444282772302L);
                add(1443605444282772303L);
                add(1L);
            }};
    }
    @Test
    public void test() {
        TestFastJson testFastJson = new TestFastJson();
        String jsonString = JSON.toJSONString(testFastJson, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"a\":\"1443605444282772302\",\"b\":[\"1443605444282772302\",\"1443605444282772303\",1]}", jsonString);
    }
}
