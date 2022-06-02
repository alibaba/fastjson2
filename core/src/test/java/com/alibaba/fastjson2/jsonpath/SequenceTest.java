package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequenceTest {
    @Test
    public void test() {
        assertEquals("[1.1,2.1]", JSONPath.extract("[[1.1,1.2],[2.1,1.2]]", "$[*][0]").toString());

        assertEquals("[1,2,3,4,5]", JSONPath.extract("[1,2,3,4,5]", "$[*][0]").toString());
        assertEquals("[[],2,3,4,5]", JSONPath.extract("[[[]],2,3,4,5]", "$[*][0]").toString());
        assertEquals("[[],[2],3,4,5]", JSONPath.extract("[[[]],[[2]],3,4,5]", "$[*][0]").toString());
        assertEquals("[[],[2],3,4,5]", JSONPath.extract("[[[]],[[2]],[3],4,5]", "$[*][0]").toString());
        assertEquals("[1,2,3]", JSONPath.extract("[[1,2],[2,3],[3,4]]", "$[*][0]").toString());
        assertEquals("{\"0\":1}", JSONPath.extract("{\"key\":{\"0\":1}}", "$[*][0]").toString());
//        assertEquals("[0,{\"\":[111]}]", JSONPath.extract("[0,{\"\":[111]}]", "$[*][0]").toString());
    }

    @Test
    public void test1() {
        assertEquals("1", JSONPath.extract("{\"key\":{\"0\":1}}", "$.key[0]").toString());
//        assertEquals("{\"\":[111]}", JSONPath.extract("[0,{\"\":[111]}]", "$[1][0]").toString());
    }
}
