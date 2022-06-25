package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue505 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 101;

        String str = JSON.toJSONString(bean, NameFilter.of(PropertyNamingStrategy.PascalCase));
        assertEquals("{\"UserId\":101}", str);
        JSONObject object = JSON.parseObject(str);
        assertEquals(101, object.get("UserId"));
    }

    public static class Bean {
        public int userId;
    }
}
