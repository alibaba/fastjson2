package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2804 {
    @Test
    public void test() throws Exception {
        String str2 = JSON.toJSONString(Role2.SYSTEM);
        assertEquals("\"system\"", str2);
        assertEquals(Role2.SYSTEM, JSON.parseObject(str2, Role2.class));
    }

    @Test
    public void testBean2() {
        Bean2 bean = new Bean2();
        bean.role = Role2.SYSTEM;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"role\":\"system\"}", str);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.role, bean1.role);
    }

    public static class Bean2 {
        public Role2 role;
    }

    public enum Role2 {
        @JSONField(name = "system")
        SYSTEM,
        @JSONField(name = "user")
        USEr,
    }
}
