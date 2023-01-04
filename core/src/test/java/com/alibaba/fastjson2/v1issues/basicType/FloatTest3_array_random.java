package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader.Feature;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatTest3_array_random {
    int loopCount = 1_000_000;
    int loopCount2 = 10_000_000;

    public FloatTest3_array_random() {
        if (System.getProperty("java.vm.name").contains("OpenJ9")) {
            this.loopCount = 1_000;
            this.loopCount2 = 10_000;
        }
    }

    @Test
    public void test_ran() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < loopCount; ++i) {
            float val = rand.nextFloat();

            String str = JSON.toJSONString(new Model(new float[]{val}));
            Model m = JSON.parseObject(str, Model.class);

            assertEquals(val, m.value[0]);
        }
    }

    @Test
    public void test_ran_2() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < loopCount2; ++i) {
            float val = rand.nextFloat();

            String str = JSON.toJSONString(new Model(new float[]{val}), JSONWriter.Feature.BeanToArray);
            Model m = JSON.parseObject(str, Model.class, Feature.SupportArrayToBean);

            assertEquals(val, m.value[0]);
        }
    }

    public static class Model {
        public float[] value;

        public Model() {
        }

        public Model(float[] value) {
            this.value = value;
        }
    }
}
