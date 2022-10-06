package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UseNativeObjectTest {
    @Test
    public void test0() {
        assertEquals(HashMap.class, JSON.parse("{}", JSONReader.Feature.UseNativeObject).getClass());
        assertEquals(HashMap.class, JSON.parseObject("{}", Object.class, JSONReader.Feature.UseNativeObject).getClass());
    }

    @Test
    public void test1() {
        String str = "{}";
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.getContext().setObjectSupplier(HashMap::new);
            assertEquals(HashMap.class, jsonReader.readObject().getClass());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.getContext().setObjectSupplier(HashMap::new);
            assertEquals(HashMap.class, jsonReader.read(Object.class).getClass());
        }
        {
            JSONReader jsonReader = JSONReader.of(str);
            jsonReader.getContext().setObjectSupplier(HashMap::new);
            assertEquals(HashMap.class, jsonReader.read(Map.class).getClass());
        }
    }

    @Test
    public void test2() {
        String str = "{}";
        {
            JSONReader jsonReader = JSONReader.of(str, JSONFactory.createReadContext(HashMap::new));
            assertEquals(HashMap.class, jsonReader.readObject().getClass());
        }
        {
            JSONReader jsonReader = JSONReader.of(str, JSONFactory.createReadContext(HashMap::new));
            assertEquals(HashMap.class, jsonReader.read(Object.class).getClass());
        }
        {
            JSONReader jsonReader = JSONReader.of(str, JSONFactory.createReadContext(HashMap::new));
            assertEquals(HashMap.class, jsonReader.read(Map.class).getClass());
        }
    }

    @Test
    public void test3() {
        assertEquals(
                HashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(HashMap::new)
                ).getClass()
        );

        assertEquals(
                HashMap.class,
                JSON.parseObject(
                        "{}",
                        Object.class,
                        JSONFactory.createReadContext(HashMap::new)
                ).getClass()
        );

        assertEquals(
                HashMap.class,
                JSON.parseObject(
                        "{}".getBytes(StandardCharsets.UTF_8),
                        Object.class,
                        JSONFactory.createReadContext(HashMap::new)
                ).getClass()
        );

        {
            Object parse = JSON.parse(
                    "[{}]",
                    JSONFactory.createReadContext(TreeMap::new, LinkedList::new)
            );
            assertEquals(LinkedList.class, parse.getClass());
            assertEquals(TreeMap.class, ((List) parse).get(0).getClass());
        }
    }
}
