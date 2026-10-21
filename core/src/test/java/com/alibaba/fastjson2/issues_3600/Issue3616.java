package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
public class Issue3616 {
    /**
     * Issue #3616: a flat list of JavaBeans whose field groups split between the
     * direct-write path (e.g. String) and the non-direct-write path (e.g. BigDecimal)
     * caused {@code JSONException: level too large} after ~2049 elements, even though
     * the structure has no real nesting. Each bean called {@code startObject()}
     * (level++) but the matching object end was emitted as a raw byte on the direct
     * path, so {@code level} never decremented.
     */
    @Test
    public void testFlatListBigDecimalAndString() {
        List<MixedBean> list = new ArrayList<>();
        for (int i = 0; i < 2100; i++) {
            list.add(new MixedBean("a" + i, new BigDecimal("1232132")));
        }

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(list));
        List<MixedBean> parsed = JSONB.parseArray(bytes, MixedBean.class);
        assertEquals(list.size(), parsed.size());
        assertEquals("a0", parsed.get(0).getD());
        assertEquals(new BigDecimal("1232132"), parsed.get(0).getC());
        assertEquals("a2099", parsed.get(2099).getD());
    }

    @Test
    public void testFlatListWithDateObject() {
        List<DateBean> list = new ArrayList<>();
        for (int i = 0; i < 2100; i++) {
            list.add(new DateBean("name" + i, new java.util.Date(0)));
        }
        assertDoesNotThrow(() -> JSONB.toBytes(list));
    }

    public static class MixedBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private String d;
        private BigDecimal c;

        public MixedBean() {
        }

        public MixedBean(String d, BigDecimal c) {
            this.d = d;
            this.c = c;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public BigDecimal getC() {
            return c;
        }

        public void setC(BigDecimal c) {
            this.c = c;
        }
    }

    public static class DateBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private java.util.Date time;

        public DateBean() {
        }

        public DateBean(String name, java.util.Date time) {
            this.name = name;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public java.util.Date getTime() {
            return time;
        }

        public void setTime(java.util.Date time) {
            this.time = time;
        }
    }
}
