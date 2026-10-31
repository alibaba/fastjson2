package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7762 {
    static final String WRAPPERS = "\"b\":false,\"by\":0,\"c\":\"\\u0000\",\"d\":0.0,\"f\":0.0,\"i\":0,\"l\":0,\"s\":0";

    static Stream<ObjectWriterCreator> creators() {
        return Stream.of(ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE);
    }

    @AfterEach
    public void tearDown() {
        JSONFactory.setContextWriterCreator(null);
        JSONFactory.getDefaultObjectWriterProvider().clear();
    }

    @ParameterizedTest
    @MethodSource("creators")
    public void wrapperDefaultValueKept(ObjectWriterCreator creator) {
        use(creator);
        assertEquals(
                "{" + WRAPPERS + "}",
                JSON.toJSONString(zero(), JSONWriter.Feature.NotWriteDefaultValue));
    }

    @ParameterizedTest
    @MethodSource("creators")
    public void wrapperNullDiscarded(ObjectWriterCreator creator) {
        use(creator);
        assertEquals(
                "{}",
                JSON.toJSONString(new Bean(), JSONWriter.Feature.NotWriteDefaultValue));
    }

    @ParameterizedTest
    @MethodSource("creators")
    public void nonDefaultValueKept(ObjectWriterCreator creator) {
        use(creator);
        assertEquals(
                "{\"b\":true,\"by\":1,\"c\":\"x\",\"d\":1.0,\"f\":1.0,\"i\":1,\"l\":1,"
                        + "\"pb\":true,\"pby\":1,\"pc\":\"x\",\"pd\":1.0,\"pf\":1.0,\"pi\":1,\"pl\":1,\"ps\":1,\"s\":1}",
                JSON.toJSONString(nonDefault(), JSONWriter.Feature.NotWriteDefaultValue));
    }

    @ParameterizedTest
    @MethodSource("creators")
    public void featureDisabled(ObjectWriterCreator creator) {
        use(creator);
        assertEquals(
                "{\"b\":false,\"by\":0,\"c\":\"\\u0000\",\"d\":0.0,\"f\":0.0,\"i\":0,\"l\":0,"
                        + "\"pb\":false,\"pby\":0,\"pc\":\"\\u0000\",\"pd\":0.0,\"pf\":0.0,\"pi\":0,\"pl\":0,\"ps\":0,\"s\":0}",
                JSON.toJSONString(zero()));
    }

    static void use(ObjectWriterCreator creator) {
        JSONFactory.setContextWriterCreator(creator);
        JSONFactory.getDefaultObjectWriterProvider().clear();
    }

    /** wrapper fields hold the primitive default values, primitive fields are left at their defaults */
    static Bean zero() {
        Bean bean = new Bean();
        bean.i = 0;
        bean.l = 0L;
        bean.d = 0D;
        bean.f = 0F;
        bean.b = false;
        bean.s = (short) 0;
        bean.by = (byte) 0;
        bean.c = '\0';
        return bean;
    }

    static Bean nonDefault() {
        Bean bean = new Bean();
        bean.i = 1;
        bean.l = 1L;
        bean.d = 1D;
        bean.f = 1F;
        bean.b = true;
        bean.s = (short) 1;
        bean.by = (byte) 1;
        bean.c = 'x';
        bean.pi = 1;
        bean.pl = 1L;
        bean.pd = 1D;
        bean.pf = 1F;
        bean.pb = true;
        bean.ps = (short) 1;
        bean.pby = (byte) 1;
        bean.pc = 'x';
        return bean;
    }

    public static class Bean {
        private Integer i;
        private Long l;
        private Double d;
        private Float f;
        private Boolean b;
        private Short s;
        private Byte by;
        private Character c;
        private int pi;
        private long pl;
        private double pd;
        private float pf;
        private boolean pb;
        private short ps;
        private byte pby;
        private char pc;

        public Integer getI() {
            return i;
        }

        public Long getL() {
            return l;
        }

        public Double getD() {
            return d;
        }

        public Float getF() {
            return f;
        }

        public Boolean getB() {
            return b;
        }

        public Short getS() {
            return s;
        }

        public Byte getBy() {
            return by;
        }

        public Character getC() {
            return c;
        }

        public int getPi() {
            return pi;
        }

        public long getPl() {
            return pl;
        }

        public double getPd() {
            return pd;
        }

        public float getPf() {
            return pf;
        }

        public boolean isPb() {
            return pb;
        }

        public short getPs() {
            return ps;
        }

        public byte getPby() {
            return pby;
        }

        public char getPc() {
            return pc;
        }
    }
}
