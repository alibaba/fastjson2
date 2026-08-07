package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.json.bvtVO.BigClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FastJsonBigClassTest {
    @Test
    public void test_big_class() {
        BigClass bigObj = new BigClass();
        String json = JSON.toJSONString(bigObj, SerializerFeature.IgnoreNonFieldGetter);
        BigClass bigClass = JSON.parseObject(json, BigClass.class);
        assertNotNull(bigClass);
    }
}
