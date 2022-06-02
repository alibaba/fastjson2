package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequenceTest {
    @Test
    public void test() {
        assertEquals("[1.1,2.1]", JSONPath.extract("[[1.1,1.2],[2.1,1.2]]", "$[*][0]").toString());
    }
}
