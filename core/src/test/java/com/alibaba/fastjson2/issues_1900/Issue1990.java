package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1990 {
    @Test
    public void test() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();

        PropertyNamingStrategy namingStrategy = provider.getNamingStrategy();
        try {
            provider.setNamingStrategy(PropertyNamingStrategy.SnakeCase);
            Bean bean = new Bean();
            bean.userId = 123;
            assertEquals("{\"user_id\":123}", JSON.toJSONString(bean));
        } finally {
            provider.setNamingStrategy(namingStrategy);
        }
    }

    public static class Bean {
        public int userId;
    }
}
