package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2480 {
    @Test
    public void test() {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.config(JSONWriter.Feature.WriteNulls, false);

        Bean bean = new Bean();
        String str = JSON.toJSONString(bean, context);
        assertEquals("{}", str);
    }

    public static class Bean {
        public String value;
    }
}
