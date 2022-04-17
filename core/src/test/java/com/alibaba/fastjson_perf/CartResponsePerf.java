package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2_vo.cart.CartResponse;

public class CartResponsePerf {
    public void test_perf() throws Exception {
        String str = null;
        CartResponse response = JSON.parseObject(str, CartResponse.class);
    }
}
