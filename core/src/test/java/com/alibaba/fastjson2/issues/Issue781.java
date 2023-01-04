package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue781 {
    @BeforeEach
    public void setUp() {
        JSON.register(java.sql.Date.class, (ObjectWriter) null);
        JSON.mixIn(java.sql.Date.class, null);
        JSON.register(java.util.Date.class, (ObjectWriter) null);
        JSON.mixIn(java.util.Date.class, null);
    }

    @AfterEach
    public void tearDown() {
        JSON.register(java.sql.Date.class, (ObjectWriter) null);
        JSON.mixIn(java.sql.Date.class, null);

        JSON.register(java.util.Date.class, (ObjectWriter) null);
        JSON.mixIn(java.util.Date.class, null);
    }

    @Test
    public void test() {
        JSON.register(java.sql.Date.class, new ObjectWriter<Date>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                Date v = (Date) object;
                jsonWriter.writeString(String.valueOf(v.getTime()));
            }
        });

        long millis = 1663595616049L;
        Bean t = new Bean();
        t.setDate(new Date(millis));

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":\"1663595616049\"}", s);
    }

    static class Bean {
        Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void test1() {
        JSON.mixIn(Date.class, DateMixin.class);

        long millis = 1663595616049L;
        Bean1 t = new Bean1();
        t.setDate(new Date(millis));

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":1663595616049}", s);
    }

    @JSONType(format = "millis")
    public static class DateMixin {
    }

    static class Bean1 {
        Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void testUtilDate() {
        JSON.register(java.util.Date.class, new ObjectWriter<Date>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                java.util.Date v = (java.util.Date) object;
                jsonWriter.writeString(String.valueOf(v.getTime()));
            }
        });

        long millis = 1663595616049L;
        Bean2 t = new Bean2();
        t.setDate(new java.util.Date(millis));

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":\"1663595616049\"}", s);
    }

    static class Bean2 {
        java.util.Date date;

        public java.util.Date getDate() {
            return date;
        }

        public void setDate(java.util.Date date) {
            this.date = date;
        }
    }

    @Test
    public void testUtilDate1() {
        JSON.mixIn(java.util.Date.class, BeanUTilDateMixin.class);

        long millis = 1663595616049L;
        BeanUTilDate1 t = new BeanUTilDate1();
        t.setDate(new java.util.Date(millis));

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":1663595616049}", s);
    }

    @JSONType(format = "millis")
    public static class BeanUTilDateMixin {
    }

    static class BeanUTilDate1 {
        java.util.Date date;

        public java.util.Date getDate() {
            return date;
        }

        public void setDate(java.util.Date date) {
            this.date = date;
        }
    }

    @Test
    public void testUtilDate3() {
        JSON.register(java.util.Date.class, new ObjectWriter<Date>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                java.util.Date v = (java.util.Date) object;
                jsonWriter.writeString(String.valueOf(v.getTime()));
            }
        });

        long millis = 1663595616049L;
        Bean3 t = new Bean3();
        t.date = new java.util.Date(millis);

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":\"1663595616049\"}", s);
    }

    static class Bean3 {
        public java.util.Date date;
    }

    @Test
    public void testUtilDate4() {
        JSON.register(java.util.Date.class, new ObjectWriter<Date>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                java.util.Date v = (java.util.Date) object;
                jsonWriter.writeString(String.valueOf(v.getTime()));
            }
        });

        long millis = 1663595616049L;
        Bean4 t = new Bean4();
        t.date = new java.util.Date(millis);

        String s = JSON.toJSONString(t);
        assertEquals("{\"date\":\"1663595616049\"}", s);
    }

    public static class Bean4 {
        public java.util.Date date;
    }
}
