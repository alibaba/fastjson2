package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanTest {
    @Test
    public void test() {
        String str = "{\"id\":123,\"strings\":[\"a\",\"b\"]}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(123, bean.id);
        assertEquals(2, bean.strings.size());
    }
}
