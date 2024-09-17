package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2959 {
    @Test
    public void test() {
        assertEquals("{\"projectNO\":null,\"orderNO\":null,\"orderType\":null,\"pN\":null,\"qty\":null,\"description\":null,\"customerCode\":null,\"customerName\":null,\"currency\":null,\"netPrice\":null,\"uSD\":null,\"rate\":null,\"orderDate\":null,\"requestDate\":null,\"planedDate\":null,\"this$0\":null}",
                JSON.toJSONString(new TobeDelivery(), JSONWriter.Feature.WriteNulls));
    }

    @Data
    @JSONType(alphabetic = false)
    public class TobeDelivery {
        private String projectNO;
        private String orderNO;
        private String orderType;
        private String pN;
        private Integer qty;
        private String description;
        private String customerCode;
        private String customerName;
        private String currency;
        private BigDecimal netPrice;
        private BigDecimal uSD;
        private Float rate;
        private Date orderDate;
        private Date requestDate;
        private Date planedDate;
    }
}
