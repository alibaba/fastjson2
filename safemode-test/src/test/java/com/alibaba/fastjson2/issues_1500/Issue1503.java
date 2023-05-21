package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1503 {
    @Test
    public void test() {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.id = 101;

        Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter("com.alibaba.fastjson2.issues_1500");
        String json = JSON.toJSONString(versionInfo, JSONWriter.Feature.WriteClassName);
        VersionInfo versionInfo1 = JSON.parseObject(json, VersionInfo.class, AUTO_TYPE_FILTER);
        assertEquals("{\"id\":101}", JSON.toJSONString(versionInfo1));
    }

    public static class VersionInfo {
        public int id;
    }
}
