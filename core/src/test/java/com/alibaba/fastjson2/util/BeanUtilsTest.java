package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * note
 *
 * @author kon, created on 2022/4/27T11:04.
 * @version 1.0.0-SNAPSHOT
 */
public class BeanUtilsTest {

    @Test
    public void declaredFields() {
        BeanUtils.declaredFields(JSONArray.class, Assertions::assertNotNull);
    }
}
