package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1660 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.values.add(new Date(1513755213202L));

        String json = JSON.toJSONString(model);
        assertEquals("{\"values\":[\"2017-12-20\"]}", json);
    }

    public static class Model {
        @JSONField(format = "yyyy-MM-dd")
        private List<Date> values = new ArrayList<Date>();

        public List<Date> getValues() {
            return values;
        }
    }
}
