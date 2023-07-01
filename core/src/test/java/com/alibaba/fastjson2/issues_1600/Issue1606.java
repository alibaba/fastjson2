package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1606 {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{\"updateTime\":null}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteMapNullValue, format = "millis")
        private Date updateTime;

        /**
         * @return the updateTime
         */
        public Date getUpdateTime() {
            return updateTime;
        }

        /**
         * @param updateTime the updateTime to set
         */
        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }
    }
}
