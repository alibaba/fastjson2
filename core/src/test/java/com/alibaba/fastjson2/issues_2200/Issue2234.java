package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2234 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = null;
        bean.f1 = null;
        bean.f2 = null;
        bean.f3 = null;
        bean.f4 = null;
        bean.f5 = null;
        bean.f6 = null;
        bean.f7 = null;

        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.FieldBased);
        assertEquals("{\n" +
                "\t\"f0\": null,\n" +
                "\t\"f1\": null,\n" +
                "\t\"f2\": null,\n" +
                "\t\"f3\": null,\n" +
                "\t\"f4\": null,\n" +
                "\t\"f5\": null,\n" +
                "\t\"f6\": null,\n" +
                "\t\"f7\": null\n" +
                "}", JSONB.toJSONString(jsonb));
    }

    static class Bean {
        private Boolean f0 = true;
        private Byte f1 = 101;
        private Short f2 = 101;
        private Integer f3 = 102;
        private Long f4 = 103L;
        private Float f5 = 103F;
        private Double f6 = 103d;
        private Character f7 = 'a';
    }
}
