package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class Issue2356 {
    @Test
    public void test() throws Exception {
        Type parameterType = Issue2356.class.getMethod("create", Class.class).getGenericParameterTypes()[0];
        JSONFactory.getDefaultObjectReaderProvider().getObjectReader(parameterType);
    }

    public static <T> T create(Class<T> objectClass) {
        return null;
    }
}
