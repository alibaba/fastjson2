package com.alibaba.fastjson2.naming;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.PropertyNamingStrategy.UpperCamelCaseWithSpaces;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpperCamelCaseWithSpacesTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"User Id\":123}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.userId, bean1.userId);
    }

    @JSONType(naming = UpperCamelCaseWithSpaces)
    public static class Bean {
        public int userId;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean._someFieldName = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"_Some Field Name\":123}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean._someFieldName, bean1._someFieldName);
    }

    @JSONType(naming = UpperCamelCaseWithSpaces)
    public static class Bean1 {
        public int _someFieldName;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.aStringField = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"a String Field\":123}", str);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.aStringField, bean1.aStringField);
    }

    @JSONType(naming = UpperCamelCaseWithSpaces)
    public static class Bean2 {
        public int aStringField;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.aURL = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"a URL\":123}", str);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.aURL, bean1.aURL);
    }

    @JSONType(naming = UpperCamelCaseWithSpaces)
    public static class Bean3 {
        public int aURL;
    }
}
