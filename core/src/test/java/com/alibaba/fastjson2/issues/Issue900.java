package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue900 {
    @Test
    public void test() {
        String data = "{\n" +
                "    \"success\": true,\n" +
                "    \"errorCode\": \"\",\n" +
                "    \"message\": \"\",\n" +
                "    \"data\": {\n" +
                "        \"id\": \"20221020144047985V4EMPSB512\",\n" +
                "        \"type\": \"3\",\n" +
                "        \"status\": \"0\"\n" +
                "    }\n" +
                "}";
        Entity entity = JSON.parseObject(data).getObject("data", Entity.class);

        assertEquals("20221020144047985V4EMPSB512", entity.id);
        assertEquals("3", entity.type);
    }

    @Data
    public class Entity {
        private String id;
        private String memberId;
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal freeChg = BigDecimal.ZERO;
        private Date createDate;
        private String rechargeType;
        private String payFrom = "1";
        private String accNumber;
        private String type;
        private String status = "0";
        private String bankOrderNo;
        private String discountConfigId;
    }
}
