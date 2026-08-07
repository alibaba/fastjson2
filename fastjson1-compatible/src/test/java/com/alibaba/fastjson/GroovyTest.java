package com.alibaba.fastjson;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroovyTest {
    @Test
    public void test_groovy() throws Exception {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);

        // A类
        Class AClass = loader.parseClass("class A {\n" + //
                "    int id\n" + //
                "}");

        // A实例
        GroovyObject a = (GroovyObject) AClass.newInstance();
        a.setProperty("id", 33);
        String textA = JSON.toJSONString(a);

        GroovyObject aa = (GroovyObject) JSON.parseObject(textA, AClass);
        assertEquals(a.getProperty("id"), aa.getProperty("id"));

        // B类，继承于A
        Class BClass = loader.parseClass("class B " +
                "\nextends A {\n" +
                "    String name\n" +
                "}");

        // B实例
        GroovyObject b = (GroovyObject) BClass.newInstance();
        b.setProperty("name", "jobs");
        String textB = JSON.toJSONString(b);
        GroovyObject bb = (GroovyObject) JSON.parseObject(textB, BClass);
        assertEquals(b.getProperty("id"), bb.getProperty("id"));
        assertEquals(b.getProperty("name"), bb.getProperty("name"));

        assertEquals("{\n" +
                "\t\"id\":0,\n" +
                "\t\"name\":\"jobs\"\n" +
                "}", JSON.toJSONString(b, true));
    }
}
