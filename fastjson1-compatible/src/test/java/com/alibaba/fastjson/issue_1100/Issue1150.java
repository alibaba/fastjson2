package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 24/04/2017.
 */
public class Issue1150 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = JSON.parseObject("{\"values\":\"\"}", Model.class);
        assertNull(model.values);
    }

    @Test
    public void test_for_issue_array() throws Exception {
        Model2 model = JSON.parseObject("{\"values\":\"\"}", Model2.class);
        assertNull(model.values);
    }

    public static class Model {
        public List values;
    }

    public static class Model2 {
        public Item[] values;
    }

    public static class Item {
    }
}
