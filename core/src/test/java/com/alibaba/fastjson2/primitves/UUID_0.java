package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

public class UUID_0 {
    UUID uuid = UUID.randomUUID();
    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        VO vo = new VO();
        vo.value = uuid;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        VO vo = new VO();
        vo.value = uuid;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("value", UUID.class, VO::getValue));
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        VO vo = new VO();
        vo.value = uuid;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_asm() throws Exception {
        ObjectWriter<VO> ow = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaderCreatorASM.INSTANCE.createObjectReader(VO.class);

        VO vo = new VO();
        vo.value = uuid;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }


    public static class VO {
        private UUID value;

        public UUID getValue() {
            return value;
        }

        public void setValue(UUID value) {
            this.value = value;
        }
    }
}
