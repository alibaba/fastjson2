package com.alibaba.fastjson2.support.solon.test.requirement.case1;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author noear 2023/10/29 created
 */
public class AsNumberTest3 {
    @Test
    public void test() throws Exception {
        String json = JSON.toJSONString(new Bean(),
                JSONWriter.Feature.WriteNullBooleanAsFalse,
                JSONWriter.Feature.WriteBooleanAsNumber);

        System.out.println(json);
        assertEquals("{\"value\":0}", json);
    }

    public static class Bean {
        public Boolean value;
    }
}
