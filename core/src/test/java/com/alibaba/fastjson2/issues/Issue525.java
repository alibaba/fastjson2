package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue525 {
    @Test
    public void test() {
        String str = "{\"qq\":123456789,\"name\":\"xxx\",\"req\":null,\"groups\":null,\"qqList\":null}";
        StartPack startPack = JSON.parseObject(str, StartPack.class);
        assertEquals(123456789, startPack.qq);
        assertEquals("xxx", startPack.name);
        assertNull(startPack.reg);
        assertNull(startPack.groups);
        assertNull(startPack.qqList);
    }

    public static class StartPack
            extends PackBase {
        /**
         * 插件名字
         */
        public String name;
        /**
         * 注册的事件
         */
        public List<Integer> reg;
        /**
         * 监听QQ群列表
         */
        public List<Long> groups;
        /**
         * 监听QQ号列表
         */
        public List<Long> qqList;
    }

    public abstract static class PackBase {
        /*
         * 运行QQ号
         */
        public long qq;
    }
}
