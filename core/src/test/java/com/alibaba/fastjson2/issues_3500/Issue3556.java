package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3556 {
    @Test
    public void test() {
        Vector v = new Vector<Integer>();
        v.add(1);
        Bean bean = new Bean();
        bean.vector = v;
        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        Bean bean1 = (Bean) JSONB.parseObject(bytes, Object.class,
                JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(v.size(), bean1.vector.size());
        assertEquals(v.get(0), bean1.vector.get(0));
    }

    public static class Bean {
        public List vector;
    }
}
