package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2066 {
    @Test
    public void test() throws Exception {
        String str = "{\"values\":[[1,2],[3,4]]}";
        Model model = JSON.parseObject(str, Model.class);
        assertEquals(1F, model.getValues().get(0)[0]);
        assertEquals(2F, model.getValues().get(0)[1]);
        assertEquals(3F, model.getValues().get(1)[0]);
        assertEquals(4F, model.getValues().get(1)[1]);

        Model model1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
        assertEquals(1F, model1.getValues().get(0)[0]);
        assertEquals(2F, model1.getValues().get(0)[1]);
        assertEquals(3F, model1.getValues().get(1)[0]);
        assertEquals(4F, model1.getValues().get(1)[1]);

        Model model2 = JSON.parseObject(str.toCharArray(), Model.class);
        assertEquals(1F, model2.getValues().get(0)[0]);
        assertEquals(2F, model2.getValues().get(0)[1]);
        assertEquals(3F, model2.getValues().get(1)[0]);
        assertEquals(4F, model2.getValues().get(1)[1]);
    }

    @Test
    public void test_1() throws Exception {
        String str = "{\"values\":[[1F,2F],[3F,4F]]}";
        Model model = JSON.parseObject(str, Model.class);
        assertEquals(1F, model.getValues().get(0)[0]);
        assertEquals(2F, model.getValues().get(0)[1]);
        assertEquals(3F, model.getValues().get(1)[0]);
        assertEquals(4F, model.getValues().get(1)[1]);

        Model model1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
        assertEquals(1F, model1.getValues().get(0)[0]);
        assertEquals(2F, model1.getValues().get(0)[1]);
        assertEquals(3F, model1.getValues().get(1)[0]);
        assertEquals(4F, model1.getValues().get(1)[1]);

        Model model2 = JSON.parseObject(str.toCharArray(), Model.class);
        assertEquals(1F, model2.getValues().get(0)[0]);
        assertEquals(2F, model2.getValues().get(0)[1]);
        assertEquals(3F, model2.getValues().get(1)[0]);
        assertEquals(4F, model2.getValues().get(1)[1]);
    }

    public static class Model {
        private List<float[]> values;

        public List<float[]> getValues() {
            return values;
        }

        public void setValues(List<float[]> values) {
            this.values = values;
        }
    }

    @Test
    public void test2() throws Exception {
        Bean bean = new Bean();
        bean.bytes = new byte[]{1, 2, 3};

        String str = com.alibaba.fastjson.JSON.toJSONString(bean);
        Bean bean1 = com.alibaba.fastjson2.JSON.parseObject(str, Bean.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(bean.bytes, bean1.bytes);
    }

    @Data
    private static class Bean {
        private byte[] bytes;
    }
}
