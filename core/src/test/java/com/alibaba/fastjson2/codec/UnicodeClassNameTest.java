package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnicodeClassNameTest {
    @Test
    public void test_0() throws Exception {
        动物 vo = new 动物();
        vo.名称 = "盒马";
        String str = JSON.toJSONString(vo);
        assertEquals("{\"名称\":\"盒马\"}", str);
        动物 vo1 = JSON.parseObject(str, 动物.class);
        assertEquals(vo.名称, vo1.名称);
    }

    public static class 动物 {
        public String 名称;
    }
}
