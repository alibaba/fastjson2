package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3724 {
    @Test
    public void test() {
        String str = "{\"isMember\":true}";
        TestData testData = JSON.parseObject(str, TestData.class);
        assertEquals(str, JSON.toJSONString(testData));
    }

    @Data
    public class TestData {
        @JSONField(name = "isMember")
        private boolean isMember;

//        @JSONField(name="isMember")
//        public void setMember(boolean isMember){
//            this.isMember = isMember;
//        }
    }
}
