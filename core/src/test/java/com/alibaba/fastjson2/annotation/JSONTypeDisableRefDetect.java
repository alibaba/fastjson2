package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTypeDisableRefDetect {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);

        byte[] jsonb = JSONB.toBytes(bean);
        Bean bean2 = JSONB.parseObject(jsonb, Bean.class);
        assertEquals(bean.id, bean2.id);

        String str1 = "{\"UserId\":123}";
        assertEquals(0, JSON.parseObject(str1, Bean.class).id);
        assertEquals(0, JSON.parseObject(str1, Bean.class, JSONReader.Feature.SupportSmartMatch).id);
    }

    @JSONType(disableSmartMatch = true, disableReferenceDetect = true, disableArrayMapping = true, disableAutoType = true)
    public static class Bean {
        @JSONField(name = "userId")
        public int id;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 123;
        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.id, bean1.id);
    }

    @JSONType(disableJSONB = true)
    public static class Bean1 {
        @JSONField(name = "userId")
        public int id;
    }
}
