package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * note
 *
 * @author kon, created on 2022/4/27T11:04.
 * @version 1.0.0-SNAPSHOT
 */
public class BeanUtilsTest {
    @Test
    public void declaredFields() {
        BeanUtils.declaredFields(JSONArray.class, Assertions::assertNotNull);
    }

    @Test
    public void fieldName() {
        assertEquals("userName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.CamelCase.name()));
        assertEquals("userName", BeanUtils.fieldName("userName", PropertyNamingStrategy.CamelCase.name()));
        assertEquals("userName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue.name()));

        assertEquals("UserName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.PascalCase.name()));
        assertEquals("UserName", BeanUtils.fieldName("userName", PropertyNamingStrategy.PascalCase.name()));

        assertEquals("user-name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDashes.name()));
        assertEquals("user-name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.LowerCaseWithDashes.name()));

        assertEquals("User-Name", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCamelCaseWithDashes.name()));
        assertEquals("User-Name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.UpperCamelCaseWithDashes.name()));

        assertEquals("user-name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.LowerCaseWithDashes.name()));
        assertEquals("user-name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDashes.name()));

        assertEquals("USER-NAME", BeanUtils.fieldName("UserName", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("USER-NAME", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("A-U-R-L", BeanUtils.fieldName("aURL", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("_SOME-FIELD-NAME", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.UpperCaseWithDashes.name()));

        assertEquals("some.field.name", BeanUtils.fieldName("someFieldName", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("user.name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("a.u.r.l", BeanUtils.fieldName("aURL", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("_some.field.name", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.LowerCaseWithDots.name()));

        assertEquals("SOME.FIELD.NAME", BeanUtils.fieldName("someFieldName", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("USER.NAME", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("A.U.R.L", BeanUtils.fieldName("aURL", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("_SOME.FIELD.NAME", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.UpperCaseWithDots.name()));
    }

    @Test
    public void subtypeOf() {
        assertEquals(Number.class, BeanUtils.subtypeOf(Number.class).getUpperBounds()[0]);
        assertEquals(Number.class, BeanUtils.subtypeOf(BeanUtils.subtypeOf(Number.class)).getUpperBounds()[0]);
    }

    @Test
    public void supertypeOf() {
        assertEquals(Number.class, BeanUtils.supertypeOf(Number.class).getLowerBounds()[0]);
        assertEquals(Number.class, BeanUtils.supertypeOf(BeanUtils.supertypeOf(Number.class)).getLowerBounds()[0]);
    }

    @Test
    public void getRecordFieldNames() {
        try {
            assertArrayEquals(new String[0], BeanUtils.getRecordFieldNames(Integer.class));
        } catch (Throwable ignored) {
            // ignored
        }
    }
}
