package com.alibaba.fastjson2.v1issues.writeAsArray;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WriteAsArray_double_private {
    @Test
    public void test_0() {
        VO vo = new VO();
        vo.setId(123D);
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[123.0,\"wenshao\"]", text);

        VO vo2 = JSON.parseObject(text, VO.class, JSONReader.Feature.SupportArrayToBean);
        assertTrue(vo.id == vo2.id);
        assertEquals(vo.name, vo2.name);
    }

    @Test
    public void test_error() {
        Exception error = null;
        try {
            JSON.parseObject("[123.A,\"wenshao\"]", VO.class, JSONReader.Feature.SupportArrayToBean);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error1() {
        Exception error = null;
        try {
            JSON.parseObject("[\"A\",\"wenshao\"]", VO.class, JSONReader.Feature.SupportArrayToBean);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error2() {
        Exception error = null;
        try {
            JSON.parseObject("[123:\"wenshao\"]", VO.class, JSONReader.Feature.SupportArrayToBean);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    public static class VO {
        private double id;
        private String name;

        public double getId() {
            return id;
        }

        public void setId(double id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
