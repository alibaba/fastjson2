package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue499 {
    @Test
    public void test() {
        String s = "{\"msg\":\"请求成功\",\"code\":2001,\"data\":{\"fScore\":\"479\"}," +
                "\"seq\":\"ab18fdffb2c34a48a4b87ec725e2e374\"}";
        TdCreditVO tdCreditVO = JSON.parseObject(s, TdCreditVO.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(tdCreditVO);
        assertNotNull(tdCreditVO.data);
        assertEquals("479", tdCreditVO.data.fScore);
    }

    @Data
    public static class TdCreditVO
            implements Serializable {
        private static final long serialVersionUID = -6643845945540456043L;
        private String msg;
        private Integer code;
        private DataVO data;
        private String seq;

        @Data
        public static class DataVO
                implements Serializable {
            private static final long serialVersionUID = 6663807798770288587L;
            @JSONField(alternateNames = "Fscore")
            private String fScore;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
