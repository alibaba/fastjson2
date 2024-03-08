package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2226 {
    @Test
    public void test() {
        Container container = new Container();
        container.data = new Bean[] {};
        String str = JSON.toJSONString(container, JSONWriter.Feature.WriteClassName);
        Container container2 = (Container) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(container2);
    }

    public static class Bean {
        public int id;
    }

    public static class Container {
        public Object data;
    }
}
