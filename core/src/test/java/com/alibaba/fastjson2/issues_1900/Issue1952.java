package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1952 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v0 = 0;
        bean.v1 = 1;
        bean.v2 = 2;
        bean.v3 = 3;
        assertEquals("[2,1,3,0]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONType(orders = {"v2", "v1", "v3", "v0"})
    public static class Bean {
        public int v0;
        public int v1;
        public int v2;
        public int v3;
    }
}
