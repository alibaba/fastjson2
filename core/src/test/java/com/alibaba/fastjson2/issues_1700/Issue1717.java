package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author kraity
 */
public class Issue1717 {
    @Test
    public void test() {
        assertThrows(JSONException.class, () -> JSON.parseArray("[jia]", int.class));
        assertThrows(JSONException.class, () -> JSON.parseArray("[jia]", long.class));
        assertThrows(JSONException.class, () -> JSON.parseArray("[jia]", Long.class));
        assertThrows(JSONException.class, () -> JSON.parseArray("[jia]", Integer.class));
        assertThrows(Exception.class, () -> JSON.parseArray("[jia]", Long.class, Integer.class));
        assertThrows(Exception.class, () -> JSON.parseArray("[1,jia]", Long.class, Integer.class));

        assertThrows(JSONException.class, () -> JSON.parseArray("[]", Long.class, Integer.class)); // element length mismatch
        assertThrows(JSONException.class, () -> JSON.parseArray("[1]", Long.class, Integer.class)); // element length mismatch
        assertDoesNotThrow(() -> JSON.parseArray("[1,2]", Long.class, Integer.class));
        assertThrows(JSONException.class, () -> JSON.parseArray("[1,2,3]", Long.class, Integer.class)); // element length mismatch
    }
}
