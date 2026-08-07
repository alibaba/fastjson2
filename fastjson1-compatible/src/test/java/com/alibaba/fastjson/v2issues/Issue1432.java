package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1432 {
    @Test
    public void test() {
        TypeUtils.compatibleWithFieldName = true;
        try {
            Bean bean = new Bean();
            bean.OrderActualAmount = new BigDecimal("12.34");
            assertEquals("{\"OrderActualAmount\":12.34}", JSON.toJSONString(bean));
        } finally {
            TypeUtils.compatibleWithFieldName = false;
            SerializeConfig.DEFAULT_PROVIDER.setNamingStrategy(
                    com.alibaba.fastjson2.PropertyNamingStrategy.CamelCase1x
            );
        }
    }

    public static class Bean {
        private BigDecimal OrderActualAmount;

        public BigDecimal getOrderActualAmount() {
            return OrderActualAmount;
        }

        public void setOrderActualAmount(BigDecimal orderActualAmount) {
            OrderActualAmount = orderActualAmount;
        }
    }
}
