package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.Int2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CopyTest {
    @Test
    public void test() {
        Object[] values = new Object[]{JSONObject.of(), JSONArray.of(1, 2, 3), 1, 1.0, BigDecimal.ONE, null, 101L, "abc", Collections.emptyList()};
        for (Object value : values) {
            Object copy = JSON.copy(value);
            assertEquals(value, copy);
        }
    }

    @Test
    public void test0() {
        Int2 value = new Int2();
        value.setV0000(1001);
        value.setV0001(1002);

        Int2 copy = JSON.copy(value);

        assertEquals(value.getV0000(), copy.getV0000());
        assertEquals(value.getV0001(), copy.getV0001());
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.v0 = 1001;
        bean.v1 = 1002;

        Bean1 copy = JSON.copy(bean, JSONWriter.Feature.FieldBased);

        assertEquals(bean.v0, copy.v0);
        assertEquals(bean.v1, copy.v1);

        Bean1 copy2 = JSON.copy(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray);
        assertEquals(bean.v0, copy2.v0);
        assertEquals(bean.v1, copy2.v1);

        Bean1 copy3 = JSON.copyTo(bean, Bean1.class, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray);
        assertEquals(bean.v0, copy3.v0);
        assertEquals(bean.v1, copy3.v1);
    }

    @Test
    public void test2() {
        Bean1 bean = new Bean1();
        bean.v0 = 1001;
        bean.v1 = 1002;
        JSONArray array = JSONArray.of(bean);

        JSONArray copy = JSON.copy(array, JSONWriter.Feature.FieldBased);
        Bean1 bean1 = (Bean1) copy.get(0);
        assertEquals(bean.v0, bean1.v0);
        assertEquals(bean.v1, bean1.v1);

        JSONArray copy2 = JSON.copy(array, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray);
        Bean1 bean2 = (Bean1) copy2.get(0);
        assertEquals(bean.v0, bean2.v0);
        assertEquals(bean.v1, bean2.v1);
    }

    public static class Bean1 {
        private int v0;
        private int v1;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.settCode("zsdqjdbdd");
        bean.setNum(1);
        Set<String> set = new HashSet<>();
        set.add("111");
        set.add("222");
        set.add("333");
        bean.setStringSet(set);
        bean.setBigDecimal(new BigDecimal(1));

        Bean3 copy = JSON.copy(bean);
        assertEquals(bean.tCode, copy.tCode);
        assertEquals(bean.num, copy.num);
        assertEquals(bean.stringSet, copy.stringSet);
        assertEquals(bean.bigDecimal, copy.bigDecimal);
    }

    public static class Bean3 {
        private String tCode;
        private int num;
        private Set<String> stringSet;
        private BigDecimal bigDecimal;

        public String gettCode() {
            return tCode;
        }

        public void settCode(String tCode) {
            this.tCode = tCode;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public Set<String> getStringSet() {
            return stringSet;
        }

        public void setStringSet(Set<String> stringSet) {
            this.stringSet = stringSet;
        }

        public BigDecimal getBigDecimal() {
            return bigDecimal;
        }

        public void setBigDecimal(BigDecimal bigDecimal) {
            this.bigDecimal = bigDecimal;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4(101);
        bean.setName("DataWorks");

        Bean4 bean1 = JSON.copy(bean);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);

        Bean4 bean2 = JSON.copyTo(bean, Bean4.class);
        assertEquals(bean.id, bean2.id);
        assertEquals(bean.name, bean2.name);
    }

    public static class Bean4 {
        private final int id;
        private String name;

        public Bean4(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.id = 101;
        bean.setName("DataWorks");

        Bean5 bean1 = JSON.copy(bean);
        assertEquals(0, bean1.id);
        assertEquals(bean.name, bean1.name);

        Bean5 bean2 = JSON.copyTo(bean, Bean5.class);
        assertEquals(0, bean2.id);
        assertEquals(bean.name, bean2.name);

        Bean6 bean3 = JSON.copyTo(bean, Bean6.class);
        assertEquals(bean.id, bean3.id);
        assertEquals(bean.name, bean3.name);

        Bean4 bean4 = JSON.copyTo(bean, Bean4.class);
        assertEquals(bean.id, bean4.id);
        assertEquals(bean.name, bean4.name);
    }

    public static class Bean5 {
        private int id;
        private String name;

        public Bean5() {
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Bean6 {
        public int id;
        public String name;
    }
}
