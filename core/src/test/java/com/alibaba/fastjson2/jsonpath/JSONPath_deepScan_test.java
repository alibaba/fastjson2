package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_deepScan_test {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test_0() throws Exception {
        Map root = Collections.singletonMap("company",
                Collections.singletonMap("departs",
                        Arrays.asList(
                                Collections.singletonMap("id",
                                        1001),
                                Collections.singletonMap("id",
                                        1002),
                                Collections.singletonMap("id", 1003) //
                        )
                ));

        List<Object> ids = (List<Object>) JSONPath.eval(root, "$..id");
        assertEquals(3, ids.size());
        assertEquals(1001, ids.get(0));
        assertEquals(1002, ids.get(1));
        assertEquals(1003, ids.get(2));
    }

    public static class Root {
    }
}
