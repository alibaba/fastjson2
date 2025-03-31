package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3444 {
    @Test
    public void test() {
        String str = "{\"flag\":1,\"msg\":\"success\",\"datatable\":[[{\"a\":1}]]}";
        Bean bean = JSON.parseObject(str, Bean.class);
        String str1 = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str1, Bean.class);
        assertEquals(bean.flag, bean1.flag);
        assertEquals(bean.msg, bean1.msg);
        assertEquals(bean.datatable, bean1.datatable);
    }

    public static class Bean {
        public int flag;
        public String msg;
        public List<List<Map<String, Object>>> datatable;
    }
}
