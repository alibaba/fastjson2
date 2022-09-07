package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue740 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"2022-09-07T12:38:31\"}", Bean.class);
        assertNotNull(bean.date);
    }

    @Data
    public class Bean{
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }
}
