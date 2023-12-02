package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2067 {
    @Test
    public void test() {
        String str = "{'code':3006,'data':{'betList':[5,10,20,50,100],'buyCards':[],'cur_round':0,'mapId':40,'my_player':'{\\'uid\\':13730840,\\'nickname\\':\\'王10\\',\\'sex\\':0,\\'headimgurl\\':\\'http://thirdwsx.qlogo.cn/mmopen/vi_32/s0tWG8WNxdfUjicicle40ODeyHBt8DttcKAw5zbgwgsEru2hMPdiasd2gH68Vz8Tx0smvlwjN2GydWj7ia4tUJPYHicA/132\\',\\'client_pos\\':0,\\'is_offline\\':false,\\'is_viewer\\':1,\\'ivpanr\\':1,\\'balance\\':1000.0,\\'totalPay\\':0.0,\\'totalPayCnt\\':0}','roomId':164509742}}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertEquals(
                JSON.parseObject(str, JSONObject.class),
                JSON.parseObject(bytes, JSONObject.class));
        assertEquals(
                JSON.parseObject(str.toCharArray(), JSONObject.class),
                JSON.parseObject(bytes, JSONObject.class));
    }
}
