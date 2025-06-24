package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2907 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.date = LocalDate.now();
        byte[] bytes = JSONB.toBytes(bean);
        Bean1 bean1 = JSONB.parseObject(bytes, Bean1.class);
        assertNotNull(bean1);
    }

    public static class Bean {
        public LocalDate date;
    }

    public static class Bean1 {
    }
}
