package com.alibaba.fastjson.cglib;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.util.JDKUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCglib {
    @Test
    public void test_cglib() throws Exception {
        if (JDKUtils.JVM_VERSION > 11) {
            return;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Entity.class);
        enhancer.setCallback(new Proxy());
        Entity entity = (Entity) enhancer.create();

        entity.setId(3);
        entity.setName("Jobs");

        String text = JSON.toJSONString(entity);
        assertEquals("{\"id\":3,\"name\":\"Jobs\"}", text);
    }

    public static class Proxy
            implements MethodInterceptor {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return proxy.invokeSuper(obj, args);
        }
    }

    public static class Entity {
        private int id;
        private String name;

        public Entity() {
        }

        public Entity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
