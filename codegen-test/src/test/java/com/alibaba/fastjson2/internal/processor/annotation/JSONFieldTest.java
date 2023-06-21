package com.alibaba.fastjson2.internal.processor.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        String str = JSON.toJSONString(bean);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);

        String str1 = "{\"UserId\":123}";
        assertEquals(0, JSON.parseObject(str1, Bean.class).id);
        assertEquals(0, JSON.parseObject(str1, Bean.class, JSONReader.Feature.SupportSmartMatch).id);
    }

    @JSONCompiled(smartMatch = false)
    public static class Bean {
        @JSONField(name = "userId")
        public int id;
    }
}
