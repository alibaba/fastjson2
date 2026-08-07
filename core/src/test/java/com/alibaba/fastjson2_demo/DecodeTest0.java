package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

public class DecodeTest0 {
    ObjectReader<VO> objectConsumer;

    public DecodeTest0() {
        objectConsumer = ObjectReaders.of(
                VO::new,
                ObjectReaders.fieldReaderBool("v0", VO::setV0),
                ObjectReaders.fieldReaderByte("v1", VO::setV1),
                ObjectReaders.fieldReaderShort("v2", VO::setV2),
                ObjectReaders.fieldReaderInt("v3", VO::setV3),
                ObjectReaders.fieldReaderLong("v4", VO::setV4),
                ObjectReaders.fieldReaderChar("v5", VO::setV5),
                ObjectReaders.fieldReaderFloat("v6", VO::setV6),
                ObjectReaders.fieldReaderDouble("v7", VO::setV7),

                ObjectReaders.fieldReader("v10", Boolean.class, VO::setV10),
                ObjectReaders.fieldReader("v11", Byte.class, VO::setV11),
                ObjectReaders.fieldReader("v12", Short.class, VO::setV12),
                ObjectReaders.fieldReader("v13", Integer.class, VO::setV13),
                ObjectReaders.fieldReader("v14", Long.class, VO::setV14),
                ObjectReaders.fieldReader("v15", Character.class, VO::setV15),
                ObjectReaders.fieldReader("v16", Float.class, VO::setV16),
                ObjectReaders.fieldReader("v17", Double.class, VO::setV17),

                ObjectReaders.fieldReader("v20", String.class, VO::setV20),
                ObjectReaders.fieldReader("v21", BigDecimal.class, VO::setV21),
                ObjectReaders.fieldReader("v22", BigInteger.class, VO::setV22),
                ObjectReaders.fieldReader("v23", UUID.class, VO::setV23),
                ObjectReaders.fieldReader("v24", Date.class, VO::setV24)
        );
    }
    @Test
    public void test_0() {
        long start = System.currentTimeMillis();

        JSONReader parser = JSONReader.of(str);
        VO vo = objectConsumer.readObject(parser, 0);

        long millis = System.currentTimeMillis() - start;
        System.out.println("asm millis : " + millis);
        System.out.println(vo.v0);
    }
    @Test
    public void test_reflect() {
        for (int i = 0; i < 1; ++i) {
            long start = System.currentTimeMillis();

            JSONReader parser = JSONReader.of(str);
            VO vo = ObjectReaders.ofReflect(VO.class)
                    .readObject(parser, 0);

            long millis = System.currentTimeMillis() - start;
            System.out.println("asm millis : " + millis);
            System.out.println(vo.v0);
        }
    }
    @Test
    public void test_invoke() {
        for (int i = 0; i < 1; ++i) {
            long start = System.currentTimeMillis();

            JSONReader parser = JSONReader.of(str);
            VO vo = ObjectReaders.of(VO.class).readObject(parser, 0);

            long millis = System.currentTimeMillis() - start;
            System.out.println("invoke millis : " + millis);
            System.out.println(vo.v0);
        }
    }
    public static class VO {
        private boolean v0;
        private byte v1;
        private short v2;
        private int v3;
        private long v4;
        private char v5;
        private float v6;
        private double v7;

        private Boolean v10;
        private Byte v11;
        private Short v12;
        private Integer v13;
        private Long v14;
        private Character v15;
        private Float v16;
        private Double v17;

        private String v20;
        private BigDecimal v21;
        private BigInteger v22;
        private UUID v23;
        private Date v24;

        public boolean isV0() {
            return v0;
        }
        public void setV0(boolean v0) {
            this.v0 = v0;
        }
        public byte getV1() {
            return v1;
        }
        public void setV1(byte v1) {
            this.v1 = v1;
        }
        public short getV2() {
            return v2;
        }
        public void setV2(short v2) {
            this.v2 = v2;
        }
        public int getV3() {
            return v3;
        }
        public void setV3(int v3) {
            this.v3 = v3;
        }
        public long getV4() {
            return v4;
        }
        public void setV4(long v4) {
            this.v4 = v4;
        }
        public char getV5() {
            return v5;
        }
        public void setV5(char v5) {
            this.v5 = v5;
        }
        public float getV6() {
            return v6;
        }
        public void setV6(float v6) {
            this.v6 = v6;
        }
        public double getV7() {
            return v7;
        }
        public void setV7(double v7) {
            this.v7 = v7;
        }
        public Boolean getV10() {
            return v10;
        }
        public void setV10(Boolean v10) {
            this.v10 = v10;
        }
        public Byte getV11() {
            return v11;
        }
        public void setV11(Byte v11) {
            this.v11 = v11;
        }
        public Short getV12() {
            return v12;
        }
        public void setV12(Short v12) {
            this.v12 = v12;
        }
        public Integer getV13() {
            return v13;
        }
        public void setV13(Integer v13) {
            this.v13 = v13;
        }

        public Long getV14() {
            return v14;
        }

        public void setV14(Long v14) {
            this.v14 = v14;
        }

        public Character getV15() {
            return v15;
        }

        public void setV15(Character v15) {
            this.v15 = v15;
        }

        public Float getV16() {
            return v16;
        }

        public void setV16(Float v16) {
            this.v16 = v16;
        }

        public Double getV17() {
            return v17;
        }

        public void setV17(Double v17) {
            this.v17 = v17;
        }

        public String getV20() {
            return v20;
        }

        public void setV20(String v20) {
            this.v20 = v20;
        }

        public BigDecimal getV21() {
            return v21;
        }

        public void setV21(BigDecimal v21) {
            this.v21 = v21;
        }

        public BigInteger getV22() {
            return v22;
        }

        public void setV22(BigInteger v22) {
            this.v22 = v22;
        }

        public UUID getV23() {
            return v23;
        }

        public void setV23(UUID v23) {
            this.v23 = v23;
        }

        public Date getV24() {
            return v24;
        }

        public void setV24(Date v24) {
            this.v24 = v24;
        }
    }

    public static final String str = "{\n" +
            "\t\"v0\" : true,\n" +
            "\t\"v1\" : 1,\n" +
            "\t\"v2\" : -123,\n" +
            "\t\"v3\" : 1234,\n" +
            "\t\"v4\" : 12345,\n" +
            "\t\"v5\" : \"a\",\n" +
            "\t\"v6\" : 12.34,\n" +
            "\t\"v7\" : -12.34,\n" +
            "\n" +
            "\t\"v10\" : true,\n" +
            "\t\"v11\" : 1,\n" +
            "\t\"v12\" : -123,\n" +
            "\t\"v13\" : 1234,\n" +
            "\t\"v14\" : 12345,\n" +
            "\t\"v15\" : \"a\",\n" +
            "\t\"v16\" : 12.34,\n" +
            "\t\"v17\" : -12.34,\n" +
            "\n" +
            "\t\"v20\" : \"abcdefg\",\n" +
            "\t\"v21\" : -123.456,\n" +
            "\t\"v22\" : 1234567890,\n" +
            "\t\"v23\" : \"483c0716-f51a-407f-a64d-5739d93e33e8\",\n" +
            "\t\"v24\" : \"2017-07-19 12:13:14\"\n" +
            "}";
}
