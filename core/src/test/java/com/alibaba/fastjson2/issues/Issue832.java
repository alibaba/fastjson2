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
        A case1 = new A();  // getAAbc(); expect: aAbc
        case1.setAAbc("aAbc");
        String fastJson1A = com.alibaba.fastjson.JSON.toJSONString(case1);
        String fastJson2A = JSON.toJSONString(case1);

        Assert.assertEquals("{\"aAbc\":\"aAbc\"}", fastJson2A);
        Assert.assertEquals(fastJson1A, fastJson2A);


        B case2 = new B();  // getAabc(); expect: aabc
        case2.setAabc("aabc");

        String fastJson1B = com.alibaba.fastjson.JSON.toJSONString(case2);
        String fastJson2B = JSON.toJSONString(case2);

        Assert.assertEquals("{\"aabc\":\"aabc\"}", fastJson2B);
        Assert.assertEquals(fastJson1B, fastJson2B);
    }
}


class A {

    private String aAbc;

    public String getAAbc() {
        return aAbc;
    }

    public void setAAbc(String aAbc) {
        this.aAbc = aAbc;
    }
}

class B {

    private String aabc;

    public String getAabc() {
        return aabc;
    }

    public void setAabc(String aabc) {
        this.aabc = aabc;
    }
}
