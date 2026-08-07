package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2355 {
    @Test
    public void test_for_issue() throws Exception {
        VO vo = new VO();
        BigDecimal num = new BigDecimal("0.00000001");
        vo.setNum(num);
        String json = JSON.toJSONString(vo);

        assertEquals("{\"num\":0.00000001}", json);
    }

    static class VO {
        @JSONField(serialzeFeatures = {SerializerFeature.WriteBigDecimalAsPlain})
        private BigDecimal num;

        public BigDecimal getNum() {
            return num;
        }

        public void setNum(BigDecimal num) {
            this.num = num;
        }
    }
}
