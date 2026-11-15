package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code JSON.copyTo(Object, Object, JSONWriter.Feature...)} — the in-place
 * overload that copies properties from a source object into an existing target object.
 *
 * @since 2.0.67
 */
@Tag("util")
public class JSON_copyToObjectTest {
    @Test
    public void copyTo_basic() {
        Source source = new Source();
        source.setId(101);
        source.setName("fastjson");
        source.setActive(true);

        Target target = new Target();
        Target result = JSON.copyTo(source, target);

        assertSame(target, result, "copyTo should return the same target instance");
        assertEquals(101, target.getId());
        assertEquals("fastjson", target.getName());
        assertTrue(target.isActive());
    }

    @Test
    public void copyTo_mapSource() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 55);
        map.put("name", "fromMap");
        map.put("active", true);

        Target target = new Target();
        assertSame(target, JSON.copyTo(map, target));
        assertEquals(55, target.getId());
        assertEquals("fromMap", target.getName());
        assertTrue(target.isActive());
    }

    @Test
    public void copyTo_mapSourceNulls() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 7);
        map.put("name", null);

        // default: null map entries are skipped, target value preserved
        Target target = new Target();
        target.setName("keep");
        JSON.copyTo(map, target);
        assertEquals(7, target.getId());
        assertEquals("keep", target.getName());

        // WriteNulls: null map entries overwrite the target
        Target overwrite = new Target();
        overwrite.setName("keep");
        JSON.copyTo(map, overwrite, JSONWriter.Feature.WriteNulls);
        assertEquals(7, overwrite.getId());
        assertNull(overwrite.getName());
    }

    @Test
    public void copyTo_nullEdgeCases() {
        // null source: target unchanged
        Target target = new Target();
        target.setId(42);
        target.setName("keep");
        assertSame(target, JSON.copyTo(null, target));
        assertEquals(42, target.getId());
        assertEquals("keep", target.getName());

        // null target: returns null on both overloads (the literal null may resolve
        // to either copyTo(Object, Class, ...) or copyTo(Object, T, ...))
        assertNull(JSON.copyTo(new Source(), null));
        assertNull(JSON.copyTo(new Source(), (Target) null));
        assertNull(JSON.copyTo(new Source(), (Class<?>) null));
    }

    @Test
    public void copyTo_unsupportedShapes() {
        // array / collection / scalar sources
        assertThrows(JSONException.class, () -> JSON.copyTo(Arrays.asList(1, 2), new Target()));
        assertThrows(JSONException.class, () -> JSON.copyTo(new int[]{1, 2}, new Target()));
        assertThrows(JSONException.class, () -> JSON.copyTo("scalar", new Target()));

        // map / collection targets
        assertThrows(JSONException.class, () -> JSON.copyTo(new Source(), new HashMap<>()));
        assertThrows(JSONException.class, () -> JSON.copyTo(new Source(), new JSONObject()));
        assertThrows(JSONException.class, () -> JSON.copyTo(new Source(), Arrays.asList(1, 2)));

        // BeanToArray feature
        assertThrows(
                JSONException.class,
                () -> JSON.copyTo(new Source(), new Target(), JSONWriter.Feature.BeanToArray));
    }

    @Test
    public void copyTo_writeEnumUsingToString() {
        EnumSource source = new EnumSource();
        source.setStatus(Status.ACTIVE);

        EnumStrTarget target = new EnumStrTarget();
        JSON.copyTo(source, target, JSONWriter.Feature.WriteEnumUsingToString);
        assertEquals(Status.ACTIVE.toString(), target.getStatus());

        EnumTarget sameTypeTarget = new EnumTarget();
        JSON.copyTo(source, sameTypeTarget);
        assertSame(Status.ACTIVE, sameTypeTarget.getStatus());

        // same-type enum target with the feature: the toString value matches back
        EnumTarget roundTripTarget = new EnumTarget();
        JSON.copyTo(source, roundTripTarget, JSONWriter.Feature.WriteEnumUsingToString);
        assertSame(Status.ACTIVE, roundTripTarget.getStatus());
    }

    @Test
    public void copyTo_ignoreErrorGetter() {
        ErrorGetterSource source = new ErrorGetterSource();
        source.setName("ok");

        // by default a throwing getter propagates
        assertThrows(JSONException.class, () -> JSON.copyTo(source, new Target()));

        // IgnoreErrorGetter skips the broken field and copies the rest
        Target target = new Target();
        JSON.copyTo(source, target, JSONWriter.Feature.IgnoreErrorGetter);
        assertEquals(0, target.getId());
        assertEquals("ok", target.getName());
    }

    @Test
    public void copyTo_fieldBased() {
        FieldBean source = new FieldBean();
        source.id = 7;
        source.name = "fb";

        // without FieldBased there are no accessible members, nothing is copied
        FieldBean plain = new FieldBean();
        JSON.copyTo(source, plain);
        assertEquals(0, plain.id);

        // with FieldBased private fields are copied
        FieldBean target = new FieldBean();
        JSON.copyTo(source, target, JSONWriter.Feature.FieldBased);
        assertEquals(7, target.id);
        assertEquals("fb", target.name);
    }

    @Test
    public void copyTo_cycleReference() {
        // a same-type self-referencing field is passed by reference, no recursion
        Node a = new Node();
        a.id = 1;
        a.next = a;

        Node b = new Node();
        b.id = 99;
        JSON.copyTo(a, b);

        assertEquals(1, b.id);
        assertSame(a, b.next, "same-type reference copied by reference, cycle preserved");
    }

    /**
     * Default: null source fields do NOT overwrite target (null excluded from JSONObject).
     * WriteNulls: null source fields DO overwrite target.
     */
    @Test
    public void copyTo_writeNulls() {
        Source source = new Source();
        source.setId(1);
        source.setName(null);
        source.setActive(true);

        // default — null field skipped, target value preserved
        Target target1 = new Target();
        target1.setName("original");
        JSON.copyTo(source, target1);
        assertEquals("original", target1.getName());
        assertEquals(1, target1.getId());

        // WriteNulls — null field overwrites target
        Target target2 = new Target();
        target2.setName("original");
        JSON.copyTo(source, target2, JSONWriter.Feature.WriteNulls);
        assertNull(target2.getName());
        assertEquals(1, target2.getId());
    }

    @Test
    public void copyTo_typeConversion() {
        BeanSource source = new BeanSource();
        source.date = LocalDate.of(2012, 12, 13);

        Bean1Target target1 = new Bean1Target();
        JSON.copyTo(source, target1);
        assertEquals("20121213", target1.date);

        Bean2Target target2 = new Bean2Target();
        JSON.copyTo(source, target2);
        assertNotNull(target2.date);
        assertEquals(
                LocalDate.of(2012, 12, 13).atStartOfDay(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                target2.date.getTime()
        );
    }

    @Test
    public void copyTo_nestedObject() {
        NestedSource source = new NestedSource();
        source.setId(1);
        Source inner = new Source();
        inner.setId(100);
        inner.setName("inner");
        inner.setActive(true);
        source.setInner(inner);

        NestedTarget target = new NestedTarget();
        JSON.copyTo(source, target);

        assertEquals(1, target.getId());
        assertNotNull(target.getInner());
        assertEquals(100, target.getInner().getId());
        assertEquals("inner", target.getInner().getName());
        assertTrue(target.getInner().isActive());
    }

    // ==================== Test beans ====================

    public static class Source {
        private int id;
        private String name;
        private boolean active;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class Target {
        private int id;
        private String name;
        private boolean active;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class NestedSource {
        private int id;
        private Source inner;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public Source getInner() { return inner; }
        public void setInner(Source inner) { this.inner = inner; }
    }

    public static class NestedTarget {
        private int id;
        private Target inner;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public Target getInner() { return inner; }
        public void setInner(Target inner) { this.inner = inner; }
    }

    public static class BeanSource {
        @JSONField(format = "yyyyMMdd")
        public LocalDate date;
    }

    public static class Bean1Target {
        public String date;
    }

    public static class Bean2Target {
        @JSONField(format = "yyyyMMdd")
        public Date date;
    }

    public enum Status {
        ACTIVE;

        @Override
        public String toString() {
            return "active-toString";
        }
    }

    public static class EnumSource {
        private Status status;

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    public static class EnumStrTarget {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class EnumTarget {
        private Status status;

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    public static class ErrorGetterSource {
        private int id = 9;
        private String name;

        public int getId() { throw new IllegalStateException("broken getter"); }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FieldBean {
        private int id;
        private String name;
    }

    public static class Node {
        public int id;
        public Node next;
    }
}
