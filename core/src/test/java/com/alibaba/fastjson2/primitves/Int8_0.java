package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Byte1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int8_0 {
    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<Byte1> ow = ObjectWriters.ofReflect(Byte1.class);
        ObjectReader<Byte1> oc = ObjectReaders.ofReflect(Byte1.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            Byte1 vo = new Byte1();
            vo.setV0000((byte) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Byte1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<Byte1> ow = ObjectWriters.objectWriter(Byte1.class);
        ObjectReader<Byte1> oc = ObjectReaders.of(Byte1.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            Byte1 vo = new Byte1();
            vo.setV0000((byte) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Byte1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<Byte1> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("v0000", Byte1::getV0000));
        ObjectReader<Byte1> oc = ObjectReaders.of(Byte1.class);

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            Byte1 vo = new Byte1();
            vo.setV0000((byte) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Byte1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }
}
