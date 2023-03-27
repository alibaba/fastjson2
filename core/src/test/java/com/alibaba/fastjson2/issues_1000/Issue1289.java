package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1289 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 1001L;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"userId\":1001}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.userId, bean1.userId);
    }

    @Data
    public static class Bean
            implements IBean {
        private Long userId;
    }

    public interface IBean {
        Long getUserId();
    }
}
