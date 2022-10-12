package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Rongzhen Yan
 */
public class Issue832 {

    @Test
    public void testCase() {
        A case1 = new A();  // getAAbc(); expect field name: aAbc
        case1.setAAbc("value");
        String fastJson1A = com.alibaba.fastjson.JSON.toJSONString(case1);
        String fastJson2A = JSON.toJSONString(case1);

        Assert.assertEquals("{\"aAbc\":\"value\"}", fastJson2A);
        Assert.assertEquals(fastJson1A, fastJson2A);


        B case2 = new B();  // getAabc(); expect field name: aabc
        case2.setAabc("value");

        String fastJson1B = com.alibaba.fastjson.JSON.toJSONString(case2);
        String fastJson2B = JSON.toJSONString(case2);

        Assert.assertEquals("{\"aabc\":\"value\"}", fastJson2B);
        Assert.assertEquals(fastJson1B, fastJson2B);

        C case3 = new C();  // getaAbc(); expect field name: aAbc
        case3.setaAbc("value");

        String fastJson1C = com.alibaba.fastjson.JSON.toJSONString(case3);
        String fastJson2C = JSON.toJSONString(case3);

        Assert.assertEquals("{\"aAbc\":\"value\"}", fastJson2C);
        Assert.assertEquals(fastJson1C, fastJson2C);
    }

    static class A {

        private String aAbc;

        public String getAAbc() {
            return aAbc;
        }

        public void setAAbc(String aAbc) {
            this.aAbc = aAbc;
        }
    }

    static class B {

        private String aabc;

        public String getAabc() {
            return aabc;
        }

        public void setAabc(String aabc) {
            this.aabc = aabc;
        }
    }

    static class C {

        private String aAbc;

        public String getaAbc() {
            return aAbc;
        }

        public void setaAbc(String aAbc) {
            this.aAbc = aAbc;
        }
    }
}
