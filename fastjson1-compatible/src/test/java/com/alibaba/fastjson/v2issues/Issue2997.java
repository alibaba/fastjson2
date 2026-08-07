package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

public class Issue2997 {
    @Test
    public void testContainsNullRootObject() {
        Object rootObject = null;
        String path = "$.key";
        JSONPath.contains(rootObject, path);
    }
}
