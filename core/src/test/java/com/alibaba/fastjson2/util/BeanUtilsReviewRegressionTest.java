package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.codec.FieldInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeanUtilsReviewRegressionTest {
    @Test
    public void longNamesCanExpandPastCachedBuffer() {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            name.append("aB");
        }
        String value = name.toString();
        assertEquals(value.replace("B", "_b"), BeanUtils.snakeCase(value, 0));
        assertEquals(value.replace("B", "_b"), BeanUtils.underScores(value, 0, false));
        assertEquals(value.replace("B", "-b"), BeanUtils.dashes(value, 0, false));
        assertEquals(value.replace("B", ".b"), BeanUtils.dots(value, 0, false));
        assertTrue(BeanUtils.upperCamelWith(value, 0, ' ').length() > 200);
    }

    @Test
    public void unwrappedAnnotationPreservesOtherFeatures() throws Exception {
        FieldInfo info = new FieldInfo();
        info.features = 1;
        BeanUtils.processJacksonJsonUnwrapped(info, UnwrappedBean.class.getField("value").getAnnotation(JsonUnwrapped.class));
        assertEquals(1 | FieldInfo.UNWRAPPED_MASK, info.features);
    }

    @Test
    public void innerParentLookupDoesNotReplacePublicFieldCache() {
        Owner owner = new Owner();
        Owner.Inner inner = owner.new Inner();
        BeanUtils.setNoneStaticMemberClassParent(inner, owner);
        Set<String> fields = new HashSet<>();
        BeanUtils.fields(Owner.Inner.class, field -> fields.add(field.getName()));
        assertEquals(1, fields.size());
        assertTrue(fields.contains("visible"));
    }

    @Test
    public void uppercaseSetterLeavesNonAsciiCharactersIntact() {
        assertEquals("A\u00e9", BeanUtils.setterName("setA\u00e9", "UpperCase"));
        assertEquals("\u00e9A", BeanUtils.setterName("set\u00e9a", "UpperCase"));
    }

    @Test
    public void declaredFieldLookupDoesNotReplacePublicFieldCache() {
        Set<String> declared = new HashSet<>();
        BeanUtils.declaredFields(Child.class, field -> declared.add(field.getName()));
        assertTrue(declared.contains("hidden"));
        Set<String> fields = new HashSet<>();
        BeanUtils.fields(Child.class, field -> fields.add(field.getName()));
        assertEquals(2, fields.size());
        assertTrue(fields.contains("inherited"));
        assertTrue(fields.contains("visible"));
    }

    public static class Parent {
        public int inherited;
    }

    public static class Child extends Parent {
        public int visible;
        private int hidden;
    }

    public static class UnwrappedBean {
        @JsonUnwrapped
        public Object value;
    }

    public static class Owner {
        public class Inner {
            public int visible;
            private int hidden;
        }
    }
}
