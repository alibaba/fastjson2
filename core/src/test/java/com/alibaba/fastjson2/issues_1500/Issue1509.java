package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1509 {
    @Test
    public void test() {
        LoginUser loginUser = new LoginUser();
        loginUser.setName("张三");
        loginUser.setRealName("zhangsan");
        loginUser.setAuthorities(new HashSet());
        byte[] asd = JSONB.toBytes(loginUser, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        ContextAutoTypeBeforeHandler contextAutoTypeBeforeHandler = new ContextAutoTypeBeforeHandler(
                "java.util.Collections$UnmodifiableSet",
                "com.alibaba.fastjson2.issues_1500"
        );

        System.out.println(JSONB.toJSONString(asd, true));

        LoginUser user1 = JSONB.parseObject(asd, LoginUser.class, contextAutoTypeBeforeHandler,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertNotNull(user1);
        assertEquals(loginUser.name, user1.name);
        assertEquals(loginUser.authorities.getClass(), user1.authorities.getClass());
    }

    @Data
    public static class LoginUser
            implements Serializable {
        private String name;
        private String realName;
        private Long orgId;
        private Set<Long> authorities;

        public void setAuthorities(Set<Long> authorities) {
            if (Objects.isNull(authorities)) {
                return;
            }
            this.authorities = Collections.unmodifiableSet(authorities);
        }
    }
}
