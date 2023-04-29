package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1424 {
    @Test
    public void test() {
        Bean object = new Bean();
        String str = JSON.toJSONString(object);
        assertEquals("{\"function\":{\"value\":\"abc\"}}", str);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertNotNull(bean1);
    }

    public static class Bean {
        public AbstractWrapper function = () -> "abc";
    }

    public interface AbstractWrapper {
        Object getValue();
    }

    public static class Bean1 {
        public AbstractWrapper1 function = (e) -> {};
    }

    public interface AbstractWrapper1 {
        void setValue(String value);
    }

    @Test
    public void test2() throws Exception {
        Class wrapperClass = TypeUtils.loadClass("com.baomidou.mybatisplus.core.toolkit.Wrappers$EmptyWrapper");
        if (wrapperClass == null) {
            return;
        }

        Constructor constructor = wrapperClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object wrapper = constructor.newInstance();
        String json = JSON.toJSONString(wrapper);
    }
}
