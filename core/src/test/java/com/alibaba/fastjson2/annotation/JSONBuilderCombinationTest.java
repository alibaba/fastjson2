package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.util.AnnotationUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link JSONBuilder}
 *
 * @author lzhpo
 */
class JSONBuilderCombinationTest {
    @Test
    void testDirectlyGet() {
        Class<DirectlyJSONBuilderPojo> clazz = DirectlyJSONBuilderPojo.class;
        JSONBuilder jsonType = AnnotationUtils.findAnnotation(clazz, JSONBuilder.class);
        assertNotNull(jsonType);
    }

    @Test
    void testCombinationGet() {
        Class<CombinationJSONBuilderPojo> clazz = CombinationJSONBuilderPojo.class;
        JSONBuilder jsonType = AnnotationUtils.findAnnotation(clazz, JSONBuilder.class);
        assertNotNull(jsonType);
    }

    @JSONBuilder
    public static class DirectlyJSONBuilderPojo {
    }

    @JSONBuilderCombination
    public static class CombinationJSONBuilderPojo {
    }

    @JSONBuilder
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface JSONBuilderCombination {
    }
}
