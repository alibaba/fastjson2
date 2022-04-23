package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 04/08/2017.
 */
public class DoubleTest {
    @Test
    public void test_obj() throws Exception {
        String json = "{\"v1\":-0.012671709,\"v2\":0.22676692048907365,\"v3\":0.13231707,\"v4\":0.80090785,\"v5\":0.6192943}";
        String json2 = "{\"v1\":\"-0.012671709\",\"v2\":\"0.22676692048907365\",\"v3\":\"0.13231707\",\"v4\":\"0.80090785\",\"v5\":\"0.6192943\"}";

        Model m1 = JSON.parseObject(json, Model.class);
        Model m2 = JSON.parseObject(json2, Model.class);

        assertNotNull(m1);
        assertNotNull(m2);

        assertEquals(-0.012671709D, m1.v1);
        assertEquals(0.22676692048907365D, m1.v2);
        assertEquals(0.13231707D, m1.v3);
        assertEquals(0.80090785D, m1.v4);
        assertEquals(0.6192943D, m1.v5);

        assertEquals(-0.012671709D, m2.v1);
        assertEquals(0.22676692048907365D, m2.v2);
        assertEquals(0.13231707D, m2.v3);
        assertEquals(0.80090785D, m2.v4);
        assertEquals(0.6192943D, m2.v5);
    }

    @Test
    public void test_array_mapping() throws Exception {
        String json = "[-0.012671709,0.22676692048907365,0.13231707,0.80090785,0.6192943]";
        String json2 = "[\"-0.012671709\",\"0.22676692048907365\",\"0.13231707\",\"0.80090785\",\"0.6192943\"]";

        Model m1 = JSON.parseObject(json, Model.class, Feature.SupportArrayToBean);
        Model m2 = JSON.parseObject(json2, Model.class, Feature.SupportArrayToBean);

        assertNotNull(m1);
        assertNotNull(m2);

        assertEquals(-0.012671709D, m1.v1);
        assertEquals(0.22676692048907365D, m1.v2);
        assertEquals(0.13231707D, m1.v3);
        assertEquals(0.80090785D, m1.v4);
        assertEquals(0.6192943D, m1.v5);

        assertEquals(-0.012671709D, m2.v1);
        assertEquals(0.22676692048907365D, m2.v2);
        assertEquals(0.13231707D, m2.v3);
        assertEquals(0.80090785D, m2.v4);
        assertEquals(0.6192943D, m2.v5);
    }

    public static class Model {
        public double v1;
        public double v2;
        public double v3;
        public double v4;
        public double v5;
    }
}
