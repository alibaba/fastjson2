package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Short1;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.writer.ObjectWriters.fieldWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int16_0 {
    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<Short1> ow = ObjectWriters.ofReflect(Short1.class);
        ObjectReader<Short1> oc = ObjectReaders.ofReflect(Short1.class);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            Short1 vo = new Short1();
            vo.setV0000((short) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Short1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<Short1> ow = ObjectWriters.objectWriter(Short1.class);
        ObjectReader<Short1> oc = ObjectReaders.of(Short1.class);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            Short1 vo = new Short1();
            vo.setV0000((short) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Short1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<Short1> ow = ObjectWriters.objectWriter(fieldWriter("v0000", Short1::getV0000));
        ObjectReader<Short1> oc = ObjectReaders.of(Short1.class);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            Short1 vo = new Short1();
            vo.setV0000((short) i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Short1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }
}
