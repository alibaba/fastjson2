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
        assertEquals("{\"projectNO\":null,\"orderNO\":null,\"orderType\":null,\"PN\":null,\"qty\":null,\"description\":null,\"customerCode\":null,\"customerName\":null,\"currency\":null,\"netPrice\":null,\"USD\":null,\"rate\":null,\"orderDate\":null,\"requestDate\":null,\"planedDate\":null,\"this$0\":null}",
                JSON.toJSONString(new TobeDelivery(), JSONWriter.Feature.WriteNulls));
    }

    @Data
    @JSONType(alphabetic = false)
    public class TobeDelivery {
        private String ProjectNO;
        private String OrderNO;
        private String OrderType;
        private String PN;
        private Integer Qty;
        private String Description;
        private String CustomerCode;
        private String CustomerName;
        private String Currency;
        private BigDecimal NetPrice;
        private BigDecimal USD;
        private Float Rate;
        private Date OrderDate;
        private Date RequestDate;
        private Date PlanedDate;
    }
}
