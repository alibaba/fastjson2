package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONBWriteNullEnumTest {
    public enum Status {
        OK, FAIL
    }

    public static class Bean {
        public Status status;
        public String name = "x";
    }

    public static class BeanGetter {
        private Status status;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    /**
     * A name distinct from Bean's "x" initializer, so that a writer which drops the field
     * instead of emitting it parses back as "x" and fails the assertion. Asserting "x" here
     * would be vacuous: the initializer alone satisfies it.
     */
    private static final String NAME = "sentinel";

    /**
     * WriteNulls promises the key is present with a null value, which a round-trip assertNull
     * cannot show: Bean.status is already null before parsing, so an omitted field passes too.
     * Pin the serialized form instead.
     */
    private static void assertStatusKeyEmitted(byte[] bytes) {
        String str = JSONB.toJSONString(bytes);
        assertTrue(str.contains("\"status\":null"), "BC_NULL emission missing, got: " + str);
    }

    /**
     * Without WriteNulls the object format must skip the null field. Asserting only that it
     * parses back null would be vacuous, so pin the absence of the key as well.
     */
    private static void assertStatusKeyOmitted(byte[] bytes) {
        String str = JSONB.toJSONString(bytes);
        assertFalse(str.contains("\"status\""), "null field should be skipped without WriteNulls, got: " + str);
    }

    /**
     * Array mapping carries fields positionally, so key presence means the slot count is kept.
     * A writer that dropped the null enum would emit a one-element array.
     */
    private static void assertArrayMappingKeepsNullSlot(byte[] bytes) {
        JSONArray array = JSON.parseArray(JSONB.toJSONString(bytes));
        assertEquals(2, array.size(), "array mapping must keep both slots, got: " + array);
        assertNull(array.get(1), "status slot must be null, got: " + array);
    }

    @Test
    public void writeNullEnumWithClassName() {
        Bean bean = new Bean();
        bean.name = NAME;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertEquals(NAME, back.name);
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void writeNullEnumWithoutClassName() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void writeNullEnumGetterBean() {
        BeanGetter bean = new BeanGetter();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        BeanGetter back = JSONB.parseObject(bytes, BeanGetter.class);
        assertNull(back.getStatus());
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void writeNullEnumMapNullValue() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteMapNullValue);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void nonNullEnumUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullEnumUsingName() {
        // The name/toString path dereferences the enum inside IO.enumCapacity, so a round
        // trip through it gates the null guard there; the ordinal path does not.
        Bean bean = new Bean();
        bean.name = NAME;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertEquals(NAME, back.name);
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void nonNullEnumUsingNameUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullEnumUsingToString() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteEnumUsingToString);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertStatusKeyEmitted(bytes);
    }

    @Test
    public void writeNullEnumBeanToArray() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNull(back.status);
        assertArrayMappingKeepsNullSlot(bytes);
    }

    @Test
    public void writeNullEnumBeanToArrayWithNulls() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNull(back.status);
        assertArrayMappingKeepsNullSlot(bytes);
    }

    @Test
    public void nonNullEnumBeanToArrayUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullsEmitsStatusKeyAsBcNull() {
        // WriteNulls must emit the status field as BC_NULL, not skip it silently.
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        String str = JSONB.toJSONString(bytes);
        assertTrue(str.contains("\"status\":null"), "BC_NULL emission missing, got: " + str);
    }

    // WriteNulls is not a precondition for the NPE. ASM's fieldValueCapacity calls
    // IO.enumCapacity for every enum field while precomputing the group's capacity, before
    // the writer decides to skip a null field, so the name/toString dereference is reached
    // even when the field is never emitted. Each of these is red without the null guards.

    @Test
    public void writeNullEnumUsingNameWithoutWriteNulls() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertStatusKeyOmitted(bytes);
    }

    @Test
    public void writeNullEnumUsingToStringWithoutWriteNulls() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteEnumUsingToString);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertStatusKeyOmitted(bytes);
    }

    @Test
    public void writeNullEnumBeanToArrayUsingNameWithoutWriteNulls() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNull(back.status);
        assertArrayMappingKeepsNullSlot(bytes);
    }
}
