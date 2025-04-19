package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3445 {
    @Test
    public void test() {
        Class targetClass = Account.class;
        Enhancer enhancer = new Enhancer();
        enhancer.setUseCache(true);
        List<Class<?>> superInterfaces = new ArrayList<Class<?>>();
        if (targetClass.isInterface()) {
            superInterfaces.add(targetClass);
            superInterfaces.add(Map.class);
        }
        enhancer.setInterfaces(superInterfaces.toArray(new Class[0]));
        enhancer.setCallback(new MethodInterceptor() {
            Map<Object, Object> map = new LinkedHashMap<>();

            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                String mname = method.getName();
                int mname_len = mname.length();

                if (mname.startsWith("set") && method.getReturnType() == void.class) {
                    map.put(mname.substring(3, mname_len).toLowerCase(), objects[0]);
                    return null;
                }
                if (mname_len == 3 && "put".equals(mname)) {
                    //非克隆对象
                    return map.put(objects[0], objects[1]);
                }
                if (mname_len == 3 && "get".equals(mname)) {
                    //非克隆对象
                    return map.get(objects[0]);
                }
                if (mname.equals("entrySet")) {
                    return map.entrySet();
                }
                if (mname.startsWith("get")) {
                    return map.get(mname.substring(3).toLowerCase());
                }
                return null;
            }
        });

        Account account = null;
        try {
            account = (Account) enhancer.create();
        } catch (Exception ignored) {
            // ignore
        }
        if (account == null) {
            return;
        }
        account.setName("test");
        account.setPassword("123");

        assertEquals("{\"name\":\"test\",\"password\":\"123\"}", JSON.toJSONString(account));
    }

    public interface Account {
        String getName();

        void setName(String name);

        String getPassword();

        void setPassword(String password);
    }
}
