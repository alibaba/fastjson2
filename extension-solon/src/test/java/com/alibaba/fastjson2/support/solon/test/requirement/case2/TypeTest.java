package com.alibaba.fastjson2.support.solon.test.requirement.case2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author noear 2023/10/29 created
 */
public class TypeTest {
    @Test
    public void test() throws Throwable {
        Bean data = new Bean();
        data.value = 12L;

        String output = JSON.toJSONString(data,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteNumberClassName);

        System.out.println(output); //{"@type":"features.type0.TypeTest$Bean","value":12L}
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.support.solon.test.requirement.case2.TypeTest$Bean\",\"value\":12}", output);
    }

    public static class Bean {
        private Long value;

        public Long getValue() {
            return value;
        }
    }
}
