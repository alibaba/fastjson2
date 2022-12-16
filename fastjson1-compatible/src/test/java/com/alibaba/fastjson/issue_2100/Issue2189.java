package com.alibaba.fastjson.issue_2100;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2189 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "[{\"id\":\"1\",\"name\":\"a\"},{\"id\":\"2\",\"name\":\"b\"}]";
        assertEquals("[\"1\",\"2\"]",
                JSONPath.extract(str, "$.*.id")
                        .toString()
        );
    }

    @Test
    public void test_for_issue_1() throws Exception {
        String str = "[{\"id\":\"1\",\"name\":\"a\"},{\"id\":\"2\",\"name\":\"b\"}]";
        assertEquals("[\"2\"]",
                JSONPath.extract(str, "$.*[?(@.name=='b')].id")
                        .toString()
        );
    }
}
