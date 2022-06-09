package com.alibaba.fastjson2.lombok;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiXiaoFeiTest {
    @Test
    public void test() {
        UserActivity userActivity = new UserActivity();
        userActivity.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(userActivity));
    }

    @Data
    public static class UserActivity {
        private long id;
    }
}
