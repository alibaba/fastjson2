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

    @Test
    public void test1() {
        String str = "{\"values\":null}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertNull(bean.values);
    }

    public static class Bean1 {
        private List<Long> values;

        public List<Long> getValues() {
            return values;
        }

        public void setValues(List<Long> values) {
            this.values = values;
        }
    }

    @Test
    public void test2() {
        String str = "{\"values\":null}";
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertNull(bean.values);
    }

    private static class Bean2 {
        private List<Long> values;

        public List<Long> getValues() {
            return values;
        }

        public void setValues(List<Long> values) {
            this.values = values;
        }
    }

    @Test
    public void test3() {
        String str = "{\"values\":null}";
        Bean3 bean = JSON.parseObject(str, Bean3.class);
        assertNull(bean.values);
    }

    public static class Bean3 {
        private List<Long> values;

        public List<Long> getValues() {
            return values;
        }
    }

    @Test
    public void test4() {
        String str = "{\"values\":null}";
        Bean4 bean = JSON.parseObject(str, Bean4.class);
        assertNull(bean.values);
    }

    public static class Bean4 {
        public List<String> values;
    }
}
