package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int128Field_1 {
    private BigInteger[] values = new BigInteger[1000];
    private int off;

    public Int128Field_1() {
        values[off++] = BigInteger.valueOf(Byte.MIN_VALUE);
        values[off++] = BigInteger.valueOf(Byte.MAX_VALUE);
        values[off++] = BigInteger.valueOf(Short.MIN_VALUE);
        values[off++] = BigInteger.valueOf(Short.MAX_VALUE);
        values[off++] = BigInteger.valueOf(Integer.MIN_VALUE);
        values[off++] = BigInteger.valueOf(Integer.MAX_VALUE);
        values[off++] = BigInteger.valueOf(Long.MIN_VALUE);
        values[off++] = BigInteger.valueOf(Long.MAX_VALUE);
        values[off++] = new BigInteger("0");
        values[off++] = new BigInteger("-1");
        values[off++] = new BigInteger("-10");
        values[off++] = new BigInteger("-100");
        values[off++] = new BigInteger("-1000");
        values[off++] = new BigInteger("-10000");
        values[off++] = new BigInteger("-100000");
        values[off++] = new BigInteger("-1000000");
        values[off++] = new BigInteger("-10000000");
        values[off++] = new BigInteger("-100000000");
        values[off++] = new BigInteger("-1000000000");
        values[off++] = new BigInteger("-10000000000");
        values[off++] = new BigInteger("-100000000000");
        values[off++] = new BigInteger("-1000000000000");
        values[off++] = new BigInteger("-10000000000000");
        values[off++] = new BigInteger("-100000000000000");
        values[off++] = new BigInteger("-1000000000000000");
        values[off++] = new BigInteger("-10000000000000000");
        values[off++] = new BigInteger("-100000000000000000");
        values[off++] = new BigInteger("-1000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000000000000000");
        values[off++] = new BigInteger("-100000000000000000000000000000000000");
        values[off++] = new BigInteger("-1000000000000000000000000000000000000");
        values[off++] = new BigInteger("-10000000000000000000000000000000000000");
        values[off++] = new BigInteger("1");
        values[off++] = new BigInteger("10");
        values[off++] = new BigInteger("100");
        values[off++] = new BigInteger("1000");
        values[off++] = new BigInteger("10000");
        values[off++] = new BigInteger("100000");
        values[off++] = new BigInteger("1000000");
        values[off++] = new BigInteger("10000000");
        values[off++] = new BigInteger("100000000");
        values[off++] = new BigInteger("1000000000");
        values[off++] = new BigInteger("10000000000");
        values[off++] = new BigInteger("100000000000");
        values[off++] = new BigInteger("1000000000000");
        values[off++] = new BigInteger("10000000000000");
        values[off++] = new BigInteger("100000000000000");
        values[off++] = new BigInteger("1000000000000000");
        values[off++] = new BigInteger("10000000000000000");
        values[off++] = new BigInteger("100000000000000000");
        values[off++] = new BigInteger("1000000000000000000");
        values[off++] = new BigInteger("10000000000000000000");
        values[off++] = new BigInteger("100000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000");
        values[off++] = new BigInteger("100000000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000000");
        values[off++] = new BigInteger("100000000000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000000000");
        values[off++] = new BigInteger("100000000000000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000000000000");
        values[off++] = new BigInteger("100000000000000000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000000000000000");
        values[off++] = new BigInteger("100000000000000000000000000000000000");
        values[off++] = new BigInteger("1000000000000000000000000000000000000");
        values[off++] = new BigInteger("10000000000000000000000000000000000000");
//
//        values[off++] = -9;
//        values[off++] = -99;
//        values[off++] = -999;
//        values[off++] = -9999;
//        values[off++] = -99999;
//        values[off++] = -999999;
//        values[off++] = -9999999;
//        values[off++] = -99999999;
//        values[off++] = -999999999;
//        values[off++] = -9999999999L;
//        values[off++] = -99999999999L;
//        values[off++] = -999999999999L;
//        values[off++] = -9999999999999L;
//        values[off++] = -99999999999999L;
//        values[off++] = -999999999999999L;
//        values[off++] = -9999999999999999L;
//        values[off++] = -99999999999999999L;
//        values[off++] = -999999999999999999L;
//        values[off++] = 9;
//        values[off++] = 99;
//        values[off++] = 999;
//        values[off++] = 9999;
//        values[off++] = 99999;
//        values[off++] = 999999;
//        values[off++] = 9999999;
//        values[off++] = 99999999;
//        values[off++] = 999999999;
//        values[off++] = 9999999999L;
//        values[off++] = 99999999999L;
//        values[off++] = 999999999999L;
//        values[off++] = 9999999999999L;
//        values[off++] = 99999999999999L;
//        values[off++] = 999999999999999L;
//        values[off++] = 9999999999999999L;
//        values[off++] = 99999999999999999L;
//        values[off++] = 999999999999999999L;
    }

    @Test
    public void test_0() throws Exception {
//        String json = "{\"value\":-10000000000}";
        String json = "{\"value\":-128}";
        JSONReader jr = JSONReader.of(json);
        VO o = jr.read(VO.class);
        assertEquals("-128", o.value.toString());
    }

    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        for (int i = 0; i < off; i++) {
            VO vo = new VO();
            vo.value = values[i];
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        for (int i = 0; i < off; i++) {
            VO vo = new VO();
            vo.value = values[i];
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("value", BigInteger.class, (VO e) -> e.value));
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        for (int i = 0; i < off; i++) {
            VO vo = new VO();
            vo.value = values[i];
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    public static class VO {
        public BigInteger value;
    }
}
