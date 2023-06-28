package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

/**
 * @author Gabriel
 * @date 2023/6/28 00:39:10
 */
public class issue1603 {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject("{\"deviceId\":\"16584\",\"srcAddress\":\"109.55.156.215\",\"ssssss\":null}");
        byte[] jsonBytes = JSON.toJSONBytes(jsonObject);
        System.out.println(new String(jsonBytes));
    }
}
