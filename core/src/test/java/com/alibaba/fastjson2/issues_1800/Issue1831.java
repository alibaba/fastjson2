package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class Issue1831 {

    @Getter
    @Setter
    @ToString
    public static class TestCls
            implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal goodsWeight;
        private BigDecimal mineGrossWeight;
        private BigDecimal mineTareWeight;
        private BigDecimal mineWeighBridgeNet;
        private BigDecimal mineNetBeforeSapmle;
        private BigDecimal mineNetAfterSapmle;
    }
    @Test
    public void Test() {
        TestCls bean = new TestCls();
        bean.setGoodsWeight(new BigDecimal("1.0009600000"));
        bean.setMineGrossWeight(new BigDecimal("1.0"));
        bean.setMineNetAfterSapmle(new BigDecimal("10.9600000"));
        bean.setMineNetBeforeSapmle(new BigDecimal("1.096"));
        bean.setMineWeighBridgeNet(new BigDecimal(".103"));
        String expected = "{\"goodsWeight\":1.0009600000,\"mineGrossWeight\":1.0," +
                "\"mineNetAfterSapmle\":10.9600000,\"mineNetBeforeSapmle\":1.096,\"" +
                "mineWeighBridgeNet\":0.103}";
        assertEquals(expected, JSONObject.toJSONString(bean));
    }
}
