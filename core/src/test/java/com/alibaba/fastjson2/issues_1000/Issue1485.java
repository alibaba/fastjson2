package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import org.junit.jupiter.api.Test;

import javax.validation.NoProviderFoundException;
import javax.validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1485 {
    @Test
    public void test() {
        ContextAutoTypeBeforeHandler typeFilter = new ContextAutoTypeBeforeHandler(true);
        assertEquals(
                NoProviderFoundException.class,
                typeFilter.apply("javax.validation.NoProviderFoundException", Object.class, JSONReader.Feature.SupportAutoType.mask)
        );
        assertEquals(
                ValidationException.class,
                typeFilter.apply("javax.validation.ValidationException", Object.class, JSONReader.Feature.SupportAutoType.mask)
        );

        assertEquals(
                NoProviderFoundException.class,
                typeFilter.apply("javax.validation.NoProviderFoundException", Exception.class, 0)
        );
        assertEquals(
                ValidationException.class,
                typeFilter.apply("javax.validation.ValidationException", Exception.class, 0)
        );
    }
}
