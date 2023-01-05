package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1054 {
    @Test
    public void test() {
        SensorSoilTemperature temperature = JSONObject.parseObject("{\"value\":24.4801}", SensorSoilTemperature.class);
        assertNotNull(temperature);
        assertEquals(new BigDecimal("24.4801"), temperature.value);
    }

    public class SensorSoilTemperature
            extends SensorModel {
        @JSONField(name = "value")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class SensorModel {
    }
}
