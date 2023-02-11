package com.alibaba.fastjson2.diff.mock;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.diff.utils.ResourceUtils;

public class MockJson {
    public MetaData load(String expectPath, String actualPath) {
        MetaData metaData = new MetaData();
        String expectContent = ResourceUtils.loadResourceLine(expectPath);
        metaData.setExpect(JSON.parse(expectContent));
        String actualContent = ResourceUtils.loadResourceLine(actualPath);
        metaData.setActual(JSON.parse(actualContent));
        return metaData;
    }
}
