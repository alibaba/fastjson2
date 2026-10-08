package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code JSON.copyTo(Object, Object, JSONWriter.Feature...)} — the in-place
 * overload that copies properties from a source object into an existing target object.
 *
 * @since 2.0.65
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
    public void copyTo_nullEdgeCases() {
        // null source: target unchanged
        Target target = new Target();
        target.setId(42);
        target.setName("keep");
        assertSame(target, JSON.copyTo(null, target));
        assertEquals(42, target.getId());
        assertEquals("keep", target.getName());

        // null target: returns null (cast to Object to select the (Object, Object) overload)
        assertNull(JSON.copyTo(new Source(), (Object) null));
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
}
