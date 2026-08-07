package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4355 {
    @Test
    public void test() {
        MemberInfo member = new MemberInfo("1234", "BIZ");
        String json = JSON.toJSONString(member);
        assertEquals("{\"bizUserId\":\"1234\",\"memberType\":\"BIZ\"}", json);
        MemberInfo memberInfo1 = JSON.parseObject(json, MemberInfo.class);
        assertEquals(memberInfo1.bizUserId, memberInfo1.bizUserId);
        assertEquals(memberInfo1.memberType, memberInfo1.memberType);
    }

    public static class MemberInfo {
        private String bizUserId;
        private String memberType;

        @JSONCreator
        public MemberInfo(String bizUserId, String memberType) {
            this.bizUserId = bizUserId;
            this.memberType = memberType;
        }

        @JSONField
        public String bizUserId() {
            return this.bizUserId;
        }

        @JSONField
        public String memberType() {
            return this.memberType;
        }
    }
}
