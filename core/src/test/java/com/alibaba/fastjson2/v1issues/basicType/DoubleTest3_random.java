package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONReader.Feature;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.RyuDouble;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest3_random {
    @Test
    public void test_extract() throws Exception {
        double val = 7.754693899073573E-4;
        String str = JSON.toJSONString(new Model(val));
        Model m = JSON.parseObject(str, Model.class);

        assertEquals(val, m.value);
    }

    @Test
    public void test_extract1() throws Exception {
        double val = 0.9825253073521191;
        String str = JSON.toJSONString(new Model(val));
        Model m = JSON.parseObject(str, Model.class);

        assertEquals(val, m.value);
    }

    @Test
    public void test_extract2() throws Exception {
        double val = 0.9221745180028585;
        String str = JSON.toJSONString(new Model(val));
        Model m = JSON.parseObject(str, Model.class);

        assertEquals(val, m.value);
    }

    @Test
    public void test_extract_1() throws Exception {
        double val = 0.21474836515489015;

        String str = JSON.toJSONString(new Model(val));
        assertEquals("{\"value\":0.21474836515489015}", str);

        {
            Model m = JSON.parseObject(str, Model.class);
            assertEquals(val, m.value);
        }

        {
            Model m = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
            assertEquals(val, m.value);
        }
    }

    @Test
    public void test_extract_2() {
        String str = "1.0006725";

        assertEquals(
                Double.parseDouble(str),
                JSONReader.of(str)
                        .readDoubleValue()
        );
    }

    @Test
    public void test_extract_3() {
        String str = "3744167620101949.3959040795987012";

        assertEquals(
                Double.parseDouble(str),
                JSONReader.of(str)
                        .readDoubleValue()
        );
    }

    @Test
    public void test_ran() {
        Random rand = new Random();

        for (int i = 0; i < 1000 * 1000 * 1; ++i) {
            double val = rand.nextDouble();

            String str = JSON.toJSONString(new Model(val));
            Model m = JSON.parseObject(str, Model.class);

            assertEquals(val, m.value);
        }
    }

    @Test
    public void test_ran_1() {
        Random rand = new Random();
        char[] chars = new char[64];
        for (int i = 0; i < 1000 * 1000 * 10; ++i) {
            double val = rand.nextDouble();
            int len = RyuDouble.toString(val, chars, 0);
            JSONReader jsonReader = JSONReader.of(chars, 0, len);
            assertEquals(val, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_ran_2() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < 1000 * 1000 * 1; ++i) {
            double val = rand.nextDouble();

            String str = JSON.toJSONString(new Model(val), JSONWriter.Feature.BeanToArray);
            Model m = JSON.parseObject(str, Model.class, Feature.SupportArrayToBean);

            assertEquals(val, m.value);
        }
    }

    @Test
    public void test_ran_3() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < 1000 * 1000 * 1; ++i) {
            double val = rand.nextDouble();

            String str = JSON.toJSONString(Collections.singletonMap("val", val));
            double val2 = JSON.parseObject(str).getDoubleValue("val");

            assertEquals(val, val2);
        }
    }

    @Test
    public void test_ran_4() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < 1000 * 1000 * 1; ++i) {
            double val = rand.nextDouble();

            HashMap map = new HashMap();
            map.put("val", val);
            String str = JSON.toJSONString(map);
            double val2 = JSON.parseObject(str).getDoubleValue("val");

            assertEquals(val, val2);
        }
    }

    public static class Model {
        public double value;

        public Model() {
        }

        public Model(double value) {
            this.value = value;
        }
    }
}
