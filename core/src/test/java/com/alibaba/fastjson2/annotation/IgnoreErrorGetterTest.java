package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreErrorGetterTest {
    @Test
    public void test_feature() throws Exception {
        Model model = new Model();
        String text = JSON.toJSONString(model, JSONWriter.Feature.IgnoreErrorGetter);
        assertEquals("{}", text);
    }

    @Test
    public void test_feature_lambda() {
        Model model = new Model();
        ObjectWriter objectWriter = TestUtils.createObjectWriterLambda(Model.class);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.IgnoreErrorGetter);
        objectWriter.write(jsonWriter, model, null, null, 0);
        assertEquals("{}", jsonWriter.toString());
    }

    public static class Model {
        public int getId() {
            throw new IllegalStateException();
        }

        public String getName() {
            throw new IllegalStateException();
        }

        public boolean getF0() {
            throw new IllegalStateException();
        }

        public byte getF1() {
            throw new IllegalStateException();
        }

        public short getF2() {
            throw new IllegalStateException();
        }

        public int getF3() {
            throw new IllegalStateException();
        }

        public long getF4() {
            throw new IllegalStateException();
        }

        public float getF5() {
            throw new IllegalStateException();
        }

        public double getF6() {
            throw new IllegalStateException();
        }

        public Boolean getF10() {
            throw new IllegalStateException();
        }

        public Byte getF11() {
            throw new IllegalStateException();
        }

        public Short getF12() {
            throw new IllegalStateException();
        }

        public Integer getF13() {
            throw new IllegalStateException();
        }

        public Long getF14() {
            throw new IllegalStateException();
        }

        public Float getF15() {
            throw new IllegalStateException();
        }

        public Double getF16() {
            throw new IllegalStateException();
        }

        public BigDecimal getF20() {
            throw new IllegalStateException();
        }

        public BigDecimal getF21() {
            throw new IllegalStateException();
        }

        public List getF22() {
            throw new IllegalStateException();
        }

        public List<String> getF23() {
            throw new IllegalStateException();
        }

        public List<Integer> getF24() {
            throw new IllegalStateException();
        }

        public Map getF25() {
            throw new IllegalStateException();
        }

        public Object getF30() {
            throw new IllegalStateException();
        }

        public Model getF31() {
            throw new IllegalStateException();
        }

        public Integer1 getF32() {
            throw new IllegalStateException();
        }
    }
}
