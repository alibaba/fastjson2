package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.KotlinUtils;
import kotlin.Metadata;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
public class Issue7749 {
    @Test
    public void serializeKotlinClassWithoutKotlinReflect() throws Exception {
        Field constructorField = KotlinUtils.class.getDeclaredField("kotlin_kclass_constructor");
        Field errorField = KotlinUtils.class.getDeclaredField("kotlin_class_klass_error");
        constructorField.setAccessible(true);
        errorField.setAccessible(true);

        synchronized (KotlinUtils.class) {
            Object constructor = constructorField.get(null);
            boolean error = errorField.getBoolean(null);
            try {
                constructorField.set(null, null);
                errorField.setBoolean(null, true);
                assertEquals("{\"sourceType\":\"Test\"}", JSON.toJSONString(new KotlinBean("Test")));
            } finally {
                constructorField.set(null, constructor);
                errorField.setBoolean(null, error);
            }
        }
    }

    @Metadata(k = 1, mv = {1, 9, 0})
    public static class KotlinBean {
        private final String sourceType;

        public KotlinBean(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceType() {
            return sourceType;
        }
    }
}
