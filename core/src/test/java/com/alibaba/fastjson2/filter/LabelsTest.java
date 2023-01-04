package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LabelsTest {
    @Test
    public void includes() {
        Bean bean = new Bean();
        bean.v0 = 100;
        bean.v1 = 101;
        bean.v2 = 102;
        bean.v3 = 103;

        String string = JSON.toJSONString(bean, Labels.includes("x"));
        assertEquals("{\"v0\":100,\"v1\":101}", string);
    }

    @Test
    public void excludes() {
        Bean bean = new Bean();
        bean.v0 = 100;
        bean.v1 = 101;
        bean.v2 = 102;
        bean.v3 = 103;

        String string = JSON.toJSONString(bean, Labels.excludes("x"));
        assertEquals("{\"v2\":102,\"v3\":103}", string);
    }

    public static class Bean {
        @JSONField(label = "x")
        public int v0;
        @JSONField(label = "x")
        public int v1;

        @JSONField(label = "y")
        public int v2;
        @JSONField(label = "y")
        public int v3;
    }
}
