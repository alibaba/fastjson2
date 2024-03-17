package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class Issue2323 {
    String errMsg = "try enabling LargeObject feature instead";

    @Test
    public void test() throws Exception {
        int size = 50;

        List<JSONObject> dataList2 = Lists.newArrayList();
        for (int rowIndex = 1; rowIndex <= 50000; rowIndex++) {
            JSONObject data = new JSONObject();
            for (int i = 0; i < size; i++) {
                data.put("test" + i, "testdata");
            }
            dataList2.add(data);
        }

        Map<String, String> params = Maps.newHashMap();
        params.put("dataListStr", JSON.toJSONString(dataList2));

        try {
            JSONWriter.ofUTF16().write(params);
        } catch (OutOfMemoryError error) {
            Assertions.assertEquals(errMsg, error.getMessage());
        }

        try {
            JSONWriter.ofUTF8().write(params);
        } catch (OutOfMemoryError error) {
            Assertions.assertEquals(errMsg, error.getMessage());
        }

        try {
            JSONWriter.ofJSONB().write(params);
        } catch (OutOfMemoryError error) {
            Assertions.assertEquals(errMsg, error.getMessage());
        }
    }
}
