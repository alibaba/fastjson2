package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.util.AnnotationUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link JSONField}
 *
 * @author lzhpo
 */
class JSONFieldCombinationTest {
    @Test
    void testDirectlyGetFromMethod() {
        Class<DirectlyJSONField> clazz = DirectlyJSONField.class;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        assertExistJSONField(declaredMethods);
    }

    @Test
    void testCombinationGetFromMethod() {
        Class<CombinationJSONFieldPojo> clazz = CombinationJSONFieldPojo.class;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        assertExistJSONField(declaredMethods);
    }

    private static void assertExistJSONField(Method[] declaredMethods) {
        Optional<JSONField> jsonField = Arrays.stream(declaredMethods)
                .map(method -> AnnotationUtils.findAnnotation(method, JSONField.class))
                .filter(Objects::nonNull)
                .findAny();
        assertTrue(jsonField.isPresent());
    }

    public static class DirectlyJSONField {
        @JSONField
        public String nickName;

        private int age;

        @JSONField
        public int getAge() {
            return age;
        }
    }

    public static class CombinationJSONFieldPojo {
        @CombinationJSONField
        public String nickName;

        private int age;

        @CombinationJSONField
        public int getAge() {
            return age;
        }
    }

    @JSONField
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface CombinationJSONField {
    }
}
