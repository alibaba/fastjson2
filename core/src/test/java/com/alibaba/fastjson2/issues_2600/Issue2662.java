package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2662 {
    @Test
    public void test() {
        final WrapperClassA wrapperClassA = new WrapperClassA();
        final PojoClassC pojoClassC = new PojoClassC();
        pojoClassC.setInt1(1);
        pojoClassC.setStr2("str");
        wrapperClassA.setPojo(pojoClassC);
        final String jsonStr = JSON.toJSONString(wrapperClassA);
        assertEquals("{\"pojo\":{\"@type\":\"com.alibaba.fastjson2.issues_2600.Issue2662$PojoClassC\",\"int1\":1,\"str2\":\"str\"}}", jsonStr);
    }

    @JSONType(serializeFeatures = {JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName})
    public static class WrapperClassA {
        private AbsPojoClassB pojo;

        public AbsPojoClassB getPojo() {
            return pojo;
        }

        public void setPojo(AbsPojoClassB pojo) {
            this.pojo = pojo;
        }
    }

    public static class AbsPojoClassB {
        private Integer int1;
        private String str2;

        public Integer getInt1() {
            return int1;
        }

        public void setInt1(Integer int1) {
            this.int1 = int1;
        }

        public String getStr2() {
            return str2;
        }

        public void setStr2(String str2) {
            this.str2 = str2;
        }
    }

    public static class PojoClassC
            extends AbsPojoClassB {
    }
}
