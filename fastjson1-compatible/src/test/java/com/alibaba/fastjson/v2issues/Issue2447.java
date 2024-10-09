package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

public class Issue2447 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setNum(10L);
        bean.setTime(new Date());
        bean.setDecimal(new BigDecimal("0.02"));
        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean);
        assert Long.class == jsonObject.get("num").getClass();
        assert Date.class == jsonObject.get("time").getClass();
        assert BigDecimal.class == jsonObject.get("decimal").getClass();
    }

    static class Bean {
        private Long num;
        private Date time;
        private BigDecimal decimal;

        public Long getNum() {
            return num;
        }

        public void setNum(Long num) {
            this.num = num;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public BigDecimal getDecimal() {
            return decimal;
        }

        public void setDecimal(BigDecimal decimal) {
            this.decimal = decimal;
        }
    }
}
