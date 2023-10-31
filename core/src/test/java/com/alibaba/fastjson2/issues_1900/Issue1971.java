package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1971 {
    @Test
    public void test() {
        String str = "{\"infos\": [[]], \"age\": 25}";
        TestA testa = com.alibaba.fastjson.JSON.parseObject(str, TestA.class);
        TestA testb = com.alibaba.fastjson2.JSON.parseObject(str, TestA.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(testa.getInfos(), testb.getInfos());
        assertEquals(testa.getAge(), testb.getAge());
    }

    public static class TestA {
        private List<Info> infos;

        private int age;

        public List<Info> getInfos() {
            return infos;
        }

        public void setInfos(List<Info> infos) {
            this.infos = infos;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class Info {
        private List name;

        public List<String> getName() {
            return name;
        }

        public void setName(List<String> name) {
            this.name = name;
        }
    }
}
