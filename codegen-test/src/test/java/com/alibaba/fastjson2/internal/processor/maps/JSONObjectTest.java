package com.alibaba.fastjson2.internal.processor.maps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = JSONObject.of("id", 1001);

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean {
        public JSONObject v01;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.v01 = JSONObject.of("id", 1001);

        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled(referenceDetect = false)
    public static class Bean1 {
        public JSONObject v01;
    }
}
