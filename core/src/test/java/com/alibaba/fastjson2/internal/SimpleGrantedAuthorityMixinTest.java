package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.internal.mixin.spring.SimpleGrantedAuthorityMixin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleGrantedAuthorityMixinTest {
    @Test
    public void test() {
        assertNotNull(new SimpleGrantedAuthorityMixin(""));
    }
}
