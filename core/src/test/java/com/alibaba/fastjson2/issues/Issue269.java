package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class Issue269 {

    @Test
    public void test() {
        String jsonStr="{\"test_prr\":2.9900000000000002131628207280300557613372802734375}";
        JSONObject json = JSON.parseObject(jsonStr, JSONReader.Feature.UseBigDecimalForDoubles);
        BigDecimal val = json.getBigDecimal("test_prr");
        System.out.println(val.doubleValue());
        System.out.println(json.toJSONString());
        System.out.println( JSON.toJSONString(json));
    }
}
