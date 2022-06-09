package com.alibaba.fastjson2.naming;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.PropertyNamingStrategy.PascalCase;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PascalCaseTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"UserId\":123}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.userId, bean1.userId);
    }

    @JSONType(naming = PascalCase)
    public static class Bean {
        public int userId;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean._someFieldName = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"_SomeFieldName\":123}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean._someFieldName, bean1._someFieldName);
    }

    @JSONType(naming = PascalCase)
    public static class Bean1 {
        public int _someFieldName;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean._someFieldName = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"_SomeFieldName\":123}", str);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean._someFieldName, bean1._someFieldName);
    }

    @JSONType(naming = PascalCase)
    public static class Bean2 {
        private int _someFieldName;

        public int get_someFieldName() {
            return _someFieldName;
        }

        public void set_someFieldName(int _someFieldName) {
            this._someFieldName = _someFieldName;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.userId = 123;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"UserId\":123}", str);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.userId, bean1.userId);
    }

    @JSONType(naming = PascalCase)
    public static class Bean3 {
        private int userId;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
