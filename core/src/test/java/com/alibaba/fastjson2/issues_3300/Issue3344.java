package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3344 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userName = "DataWorks";
        String jsonString = JSON.toJSONString(bean, NameFilter.of(PropertyNamingStrategy.SnakeCase));
        assertEquals("{\"user_name\":\"DataWorks\"}", jsonString);
    }

    public static class Bean {
        public String userName;
    }
}
