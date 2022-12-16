package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4298 {
    @Test
    public void test() {
        List<Object> list = new ArrayList<>();
        list.add("RE1220045");
        list.add("203");
        list.add(1);
        list.add("RE1220045");

        assertEquals(
                "[\"RE1220045\",\"203\",1,\"RE1220045\"]",
                JSON.toJSONString(list, JSONWriter.Feature.ReferenceDetection)
        );
    }
}
