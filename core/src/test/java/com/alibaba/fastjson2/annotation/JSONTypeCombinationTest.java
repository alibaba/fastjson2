package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.util.BeanUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link JSONType}
 *
 * @author lzhpo
 */
class JSONTypeCombinationTest {
    @Test
    void testDirectlyGetFromClassAndAnnotation() {
        Class<DirectlyJSONTypePojo> clazz = DirectlyJSONTypePojo.class;
        JSONType jsonTypeFromClass = BeanUtils.findAnnotation(clazz, JSONType.class);
        assertNotNull(jsonTypeFromClass);

        Annotation[] annotations = clazz.getAnnotations();
        Optional<JSONType> jsonTypeFromAnnotation = Arrays.stream(annotations)
                .map(annotation -> BeanUtils.findAnnotation(annotation, JSONType.class))
                .filter(Objects::nonNull)
                .findAny();
        assertTrue(jsonTypeFromAnnotation.isPresent());
    }

    @Test
    void testCombinationGetJSONBuilderFromClassAndAnnotation() {
        Class<CombinationJSONTypePojo> clazz = CombinationJSONTypePojo.class;
        JSONType jsonTypeFromClass = BeanUtils.findAnnotation(clazz, JSONType.class);
        assertNotNull(jsonTypeFromClass);

        Annotation[] annotations = clazz.getAnnotations();
        Optional<JSONType> jsonTypeFromAnnotation = Arrays.stream(annotations)
                .map(annotation -> BeanUtils.findAnnotation(annotation, JSONType.class))
                .filter(Objects::nonNull)
                .findAny();
        assertTrue(jsonTypeFromAnnotation.isPresent());
    }

    @JSONType
    public static class DirectlyJSONTypePojo {
    }

    @JSONTypeCombination
    public static class CombinationJSONTypePojo {
    }

    @JSONType
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface JSONTypeCombination {
    }
}
