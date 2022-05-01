package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int8Value_0 {
    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (byte) i;
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

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (byte) i;
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_reflect_utf8() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (byte) i;
            JSONWriter w = JSONWriter.ofUTF8();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json.getBytes(StandardCharsets.UTF_8));
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_lambda_utf8() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (byte) i;
            JSONWriter w = JSONWriter.ofUTF8();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json.getBytes(StandardCharsets.UTF_8));
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("value", VO::getValue));
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (byte) i;
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    public static class VO {
        private byte value;

        public byte getValue() {
            return value;
        }

        public void setValue(byte value) {
            this.value = value;
        }
    }
}
