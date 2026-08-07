package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("jsonpath")
public class JSONPathExistsTest {
    @Test
    public void test() {
        JSONPath path = JSONPath.of("[?exists(@.Data.user_info.userid)]");
        assertEquals("[?exists(@.Data.user_info.userid)]", path.toString());

        JSONPathSegment segment = ((JSONPathSingle) path).segment;
        assertEquals("exists(@.Data.user_info.userid)", segment.toString());
    }
}
