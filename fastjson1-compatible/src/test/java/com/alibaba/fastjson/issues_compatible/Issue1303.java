package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1303 {
    @Test
    public void test() {
        TypeUtils.compatibleWithFieldName = true;
        try {
            Bean bean = new Bean();
            bean.SPOrderSerialId = "101";
            String str = JSON.toJSONString(bean);
            assertEquals("{\"SPOrderSerialId\":\"101\"}", str);
            assertNull(SerializeConfig.DEFAULT_PROVIDER.getNamingStrategy());
        } finally {
            TypeUtils.compatibleWithFieldName = false;
            SerializeConfig.DEFAULT_PROVIDER.setNamingStrategy(
                    com.alibaba.fastjson2.PropertyNamingStrategy.CamelCase1x
            );
        }
    }

    public static class Bean {
        private String SPOrderSerialId;

        public String getSPOrderSerialId() {
            return SPOrderSerialId;
        }

        public void setSPOrderSerialId(String SPOrderSerialId) {
            this.SPOrderSerialId = SPOrderSerialId;
        }
    }
}
