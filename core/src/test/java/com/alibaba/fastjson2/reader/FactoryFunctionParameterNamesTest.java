package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactoryFunctionParameterNamesTest {
    @Test
    public void omittedNamesUseReflectionParameterNames() throws Exception {
        Method method = Bean.class.getMethod("create", String.class, String.class);
        FactoryFunction<Bean> function = new FactoryFunction<>(method);
        String first = method.getParameters()[0].getName();
        String second = method.getParameters()[1].getName();
        Map<Long, Object> values = new HashMap<>();
        values.put(Fnv.hashCode64(first), "first");
        values.put(Fnv.hashCode64(second), "second");
        assertEquals("first:second", function.apply(values).value);
        assertArrayEquals(new String[] {first, second}, function.paramNames);
    }

    @Test
    public void partialNamesUseReflectionForRemainingParameters() throws Exception {
        Method method = Bean.class.getMethod("create", String.class, String.class);
        String[] names = {"explicit"};
        FactoryFunction<Bean> function = new FactoryFunction<>(method, names);
        String second = method.getParameters()[1].getName();
        Map<Long, Object> values = new HashMap<>();
        values.put(Fnv.hashCode64("explicit"), "first");
        values.put(Fnv.hashCode64(second), "second");
        assertEquals("first:second", function.apply(values).value);
        assertArrayEquals(new String[] {"explicit", second}, function.paramNames);
        assertArrayEquals(new String[] {"explicit"}, names);
    }

    public static class Bean {
        public final String value;

        private Bean(String value) {
            this.value = value;
        }

        public static Bean create(String first, String second) {
            return new Bean(first + ':' + second);
        }
    }
}
