package com.alibaba.fastjson.issue_1500;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;
import org.junit.Assert;

public class Issue1500 extends TestCase {

    public void test_for_issue() throws Exception {

        // test aa
        Aa aa = new Aa();
        aa.setName("aa");
        String jsonAa = JSON.toJSONString(aa);
        System.out.println(jsonAa);

        Aa aa1 = JSON.parseObject(jsonAa, Aa.class);
        Assert.assertEquals("aa",aa1.getName());

        // test C
        C c = new C();
        c.setE(aa);
        String jsonC = JSON.toJSONString(c, SerializerFeature.WriteClassName);
        C c2 = JSON.parseObject(jsonC, C.class);
        Assert.assertEquals("Aa",c2.getE().getClass().getSimpleName());
        Assert.assertEquals("aa",((Aa)c2.getE()).getName());
    }

    public static class Aa extends Exception {

        public Aa(){
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public static class C {

        private Exception e;

        public Exception getE() {
            return e;
        }

        public void setE(Exception e) {
            this.e = e;
        }

    }

}
