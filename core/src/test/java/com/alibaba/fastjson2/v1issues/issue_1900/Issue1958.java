package com.alibaba.fastjson2.v1issues.issue_1900;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1958 {
    @Test
    public void test() {
        Bean bean = new Bean();
        List<Bean> list = new ArrayList<>();
        list.add(bean);
        JSONObject obj = new JSONObject();
        obj.put("list", bean);
        assertEquals("{\"list\":{\"value\":10.0100}}", obj.toString());
    }

    @Data
    public class Bean {
        BigDecimal value = new BigDecimal("10.0100");
    }
}
