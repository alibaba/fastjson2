package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3883 {
    @Test
    public void jsonReaderUTF8ArrayOOBTest() {
        assertThrows(JSONException.class, () -> JSON.parse(new String("\0.")));
    }
}
