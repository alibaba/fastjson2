package com.alibaba.fastjson2.spring;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

public class SpringRefreshScopeTest {
    // Mock interface for Spring RefreshScope
    public interface RefreshScope {
    }

    // Mock interface for configuration
    public interface LlmSystemInfoConfig {
        String getName();
        void setName(String name);
        int getValue();
        void setValue(int value);
    }

    // Mock implementation
    public static class LlmSystemInfoConfigImpl implements LlmSystemInfoConfig {
        private String name = "test";
        private int value = 42;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public void setValue(int value) {
            this.value = value;
        }
    }

    // Mock InvocationHandler for proxy objects
    public static class MockInvocationHandler implements InvocationHandler {
        private final Object target;

        public MockInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(target, args);
        }
    }

    // Create a mock interface that simulates Spring RefreshScope proxy
    public interface MockRefreshScopeInterface extends RefreshScope {
    }

    @Test
    public void testRefreshScopeInterface() {
        // Test interface detection with a class that implements local RefreshScope
        Class<?> proxyClass = Proxy.getProxyClass(
                LlmSystemInfoConfig.class.getClassLoader(),
                new Class[]{LlmSystemInfoConfig.class, RefreshScope.class});

        // Check what interfaces the proxy has
        boolean hasRefreshScope = false;
        for (Class<?> intf : proxyClass.getInterfaces()) {
            if (intf == RefreshScope.class) {
                hasRefreshScope = true;
                break;
            }
        }

        assertTrue(hasRefreshScope, "Proxy should implement RefreshScope interface");
        assertEquals(2, proxyClass.getInterfaces().length, "Proxy should implement 2 interfaces");
    }

    @Test
    public void testEnhancedProxyDetection() {
        // Test that normal classes are not detected as proxies
        assertFalse(TypeUtils.isProxy(Object.class));
        assertFalse(TypeUtils.isProxy(String.class));
    }

    @Test
    public void testSerializeProxyObject() {
        // Create a normal object
        LlmSystemInfoConfig config = new LlmSystemInfoConfigImpl();
        String json1 = JSON.toJSONString(config);

        // Create a proxy object (simulating Spring RefreshScope)
        LlmSystemInfoConfig proxy = (LlmSystemInfoConfig) Proxy.newProxyInstance(
                LlmSystemInfoConfig.class.getClassLoader(),
                new Class[]{LlmSystemInfoConfig.class, RefreshScope.class},
                new MockInvocationHandler(config));

        // Serialize the proxy object
        String json2 = JSON.toJSONString(proxy);

        // Both should produce the same JSON
        assertEquals(json1, json2);
    }

    @Test
    public void testObjectWriterProviderWithProxy() {
        ObjectWriterProvider provider = new ObjectWriterProvider();

        // Test with normal class
        provider.getObjectWriter(LlmSystemInfoConfigImpl.class);

        // Test with proxy class
        Class<?> proxyClass = Proxy.getProxyClass(
                LlmSystemInfoConfig.class.getClassLoader(),
                new Class[]{LlmSystemInfoConfig.class, RefreshScope.class});

        // This should not throw an exception and should return a valid writer
        assertDoesNotThrow(() -> {
            provider.getObjectWriter(proxyClass);
        });
    }
}
