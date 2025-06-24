package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

public class Issue3090 {
    public record GoodUpdatedCheck(boolean isUpgrade, boolean isDowngrade, boolean isChangeSubscriptionGood) {}
    @Test
    public void test() {
        GoodUpdatedCheck r = new GoodUpdatedCheck(true, false, false);
        String str = JSON.toJSONString(r);
        GoodUpdatedCheck r1 = JSON.parseObject(str, GoodUpdatedCheck.class);
    }
}
