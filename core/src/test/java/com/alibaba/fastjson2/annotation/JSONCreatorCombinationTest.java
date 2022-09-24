package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.util.AnnotationUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link JSONCreator}
 *
 * @author lzhpo
 */
class JSONCreatorCombinationTest {
    @Test
    void testDirectlyGetFromClass() {
        Class<DirectlyJSONCreatorPojo> clazz = DirectlyJSONCreatorPojo.class;
        Constructor<?>[] constructors = clazz.getConstructors();

        assertExistJSONCreator(constructors);
    }

    @Test
    void testDirectlyGetFromAnnotationType() {
        Class<DirectlyJSONCreatorPojo> clazz = DirectlyJSONCreatorPojo.class;
        Constructor<?>[] constructors = clazz.getConstructors();

        List<Annotation> annotations = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            annotations.addAll(Arrays.asList(constructor.getAnnotations()));
        }

        assertExistJSONCreator(annotations);
    }

    @Test
    void testCombinationGetFromClass() {
        Class<CombinationJSONCreatorPojo> clazz = CombinationJSONCreatorPojo.class;
        Constructor<?>[] constructors = clazz.getConstructors();

        assertExistJSONCreator(constructors);
    }

    @Test
    void testCombinationGetFromAnnotationType() {
        Class<CombinationJSONCreatorPojo> clazz = CombinationJSONCreatorPojo.class;
        Constructor<?>[] constructors = clazz.getConstructors();

        List<Annotation> annotations = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            annotations.addAll(Arrays.asList(constructor.getAnnotations()));
        }

        assertExistJSONCreator(annotations);
    }

    private static void assertExistJSONCreator(Constructor<?>[] constructors) {
        Optional<JSONCreator> jsonCreator = Arrays.stream(constructors)
                .map(constructor -> AnnotationUtils.findAnnotation(constructor, JSONCreator.class))
                .filter(Objects::nonNull)
                .findAny();

        assertTrue(jsonCreator.isPresent());
    }

    private static void assertExistJSONCreator(List<Annotation> annotations) {
        Optional<JSONCreator> jsonCreator = annotations.stream()
                .map(annotation -> {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType != JSONCreator.class) {
                        return AnnotationUtils.findAnnotation(annotationType, JSONCreator.class);
                    } else {
                        return (JSONCreator) annotation;
                    }
                })
                .filter(Objects::nonNull)
                .findAny();

        assertTrue(jsonCreator.isPresent());
    }

    public static class DirectlyJSONCreatorPojo {
        private String nickName;
        private int age;

        @JSONCreator(parameterNames = {"nickName", "age"})
        public DirectlyJSONCreatorPojo(String nickName, int age) {
            this.nickName = nickName;
            this.age = age;
        }
    }

    public static class CombinationJSONCreatorPojo {
        private String nickName;
        private int age;

        @JSONCreatorCombination
        public CombinationJSONCreatorPojo(String nickName, int age) {
            this.nickName = nickName;
            this.age = age;
        }
    }

    @JSONCreator(parameterNames = {"nickName", "age"})
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE})
    public @interface JSONCreatorCombination {
    }
}
