package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONB;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1805 {
    @Test
    public void test() {
        CodePayBO bo = new CodePayBO();
        bo.amt = BigDecimal.valueOf(1234, 2);
        bo.payMethod = PayMethodEnum.WX;

        byte[] bytes = JSONB.toBytes(bo);

        CodePayBO bo1 = JSONB.parseObject(bytes, CodePayBO.class);
        assertEquals(bo.amt, bo1.amt);
        assertEquals(bo.payMethod, bo1.payMethod);
    }

    @Data
    public static class CodePayBO {
        private BigDecimal amt;
        private PayMethodEnum payMethod;
    }

    public enum PayMethodEnum {
        WX(0), ALIPAY(1);

        @EnumValue
        @JsonValue
        public final int value;

        PayMethodEnum(int value) {
            this.value = value;
        }
    }
}
