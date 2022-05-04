package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnwrappedTest {
    @Test
    public void test() {
        ExtendableBean bean = new ExtendableBean();
        bean.name = "My bean";
        bean.properties.put("attr1", "val1");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", str);
    }

    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties = new HashMap<>();

        public ExtendableBean() {

        }

        @JSONField(unwrapped = true)
        public Map<String, String> getProperties() {
            return properties;
        }

    }
}
