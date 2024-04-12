package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Issue2431 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setB((byte)1);
        bean.setBs(Arrays.asList((byte)1));
        bean.setS((short)1);
        bean.setSs(Arrays.asList((short)1));
        bean.setI(1);
        bean.setIs(Arrays.asList(1));
        bean.setL(1L);
        bean.setLs(Arrays.asList(1L));
        bean.setF(1.0F);
        bean.setFs(Arrays.asList(1.0F));
        bean.setD(1.0);
        bean.setDs(Arrays.asList(1.0));
        assertEquals(
                "{\"b\":\"1\",\"bs\":[\"1\"],\"d\":\"1.0\",\"ds\":[\"1.0\"],\"f\":\"1.0\",\"fs\":[\"1.0\"],\"i\":\"1\",\"is\":[\"1\"],\"l\":\"1\",\"ls\":[\"1\"],\"s\":\"1\",\"ss\":[\"1\"]}",
                JSON.toJSONString(bean));
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean {
        private Byte b;
        private List<Byte> bs;
        private Short s;
        private List<Short> ss;
        private Integer i;
        private List<Integer> is;
        private Long l;
        private List<Long> ls;
        private Float f;
        private List<Float> fs;
        private Double d;
        private List<Double> ds;
    }
}
