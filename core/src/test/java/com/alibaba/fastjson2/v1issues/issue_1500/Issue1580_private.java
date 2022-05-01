package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1580_private {
    @Test
    public void test_for_issue() throws Exception {
        SimplePropertyPreFilter classAFilter = new SimplePropertyPreFilter(Model.class, "code");
        Filter[] filters = new Filter[]{classAFilter};

        Model model = new Model();
        model.code = 1001;
        model.name = "N1";

        String json = JSON.toJSONString(model, filters, JSONWriter.Feature.BeanToArray);
        assertEquals("[1001,null]", json);
    }

    private static class Model {
        private int code;
        private String name;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
