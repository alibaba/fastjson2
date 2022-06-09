package com.alibaba.fastjson.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyNamingStrategyTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 123;

        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.UpperCamelCaseWithDots;

        String string = JSON.toJSONString(bean, config);
        assertEquals("{\"User.Id\":123}", string);
    }

    public static class Bean {
        public int userId;
    }
}
