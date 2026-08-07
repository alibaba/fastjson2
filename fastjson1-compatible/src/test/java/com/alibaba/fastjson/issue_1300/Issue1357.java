package com.alibaba.fastjson.issue_1300;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Created by wenshao on 31/07/2017.
 */
public class Issue1357 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "{\"d2\":null}";
        Test2Bean b = JSONObject.parseObject(str, Test2Bean.class);
        System.out.println(b);
    }

    public static class Test2Bean {
        private LocalDateTime d2;
    }
}
