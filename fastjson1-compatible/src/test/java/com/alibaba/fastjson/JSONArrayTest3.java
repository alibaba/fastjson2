package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONArrayTest3 {
    @Test
    public void test_0() throws Exception {
        JSONArray array = new JSONArray();
        array.set(1, "1001");
        assertEquals(2, array.size());
        assertNull(array.get(0));
        assertEquals("1001", array.get(1));

        array.clear();
        assertEquals(0, array.size());

        array.set(-1, "1001");
        assertEquals(1, array.size());
        assertEquals("1001", array.get(0));

        array.fluentAdd("1002").fluentClear();
        assertEquals(0, array.size());

        array.fluentAdd("1002").fluentRemove("1002");
        assertEquals(0, array.size());

        array.fluentAdd("1002").fluentRemove(0);
        assertEquals(0, array.size());

        array.fluentSet(1, "1001");
        assertEquals(2, array.size());
        assertNull(array.get(0));
        assertEquals("1001", array.get(1));

        array.fluentRemoveAll(Arrays.asList(null, "1001"));
        assertEquals(0, array.size());

        array.fluentAddAll(Arrays.asList("1001", "1002", "1003"));
        assertEquals(3, array.size());

        array.retainAll(Arrays.asList("1002", "1004"));
        assertEquals(1, array.size());
        assertEquals("1002", array.get(0));
    }
}
