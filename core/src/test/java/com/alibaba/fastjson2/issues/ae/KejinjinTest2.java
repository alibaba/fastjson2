package com.alibaba.fastjson2.issues.ae;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KejinjinTest2 {
    @Test
    public void test() {
        Bean1 bean = new Bean1();
        bean.setId(1001);
        assertEquals("{}", JSON.toJSONString(bean));
    }

    @Data
    public static class Bean {
        @JSONField(serialize = false)
        private int id;
    }

    public static class Bean1
            extends Bean {
    }
}
