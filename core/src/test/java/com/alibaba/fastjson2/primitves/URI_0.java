package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.JSONBDump;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URI_0 {
    URI uri;

    public URI_0() throws Exception {
        uri = new URI("http://a.b.com");
    }

    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        VO vo = new VO();
        vo.value = uri;
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
        vo.value = uri;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("value", URI.class, VO::getValue));
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        VO vo = new VO();
        vo.value = uri;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_asm() throws Exception {
        ObjectWriter<VO> ow = TestUtils.WRITER_CREATOR.createObjectWriter(VO.class);
        ObjectReader<VO> oc = TestUtils.READER_CREATOR.createObjectReader(VO.class);

        VO vo = new VO();
        vo.value = uri;
        JSONWriter w = JSONWriter.of();
        ow.write(w, vo);

        String json = w.toString();
        JSONReader jr = JSONReader.of(json);
        VO o = oc.readObject(jr, 0);
        assertEquals(vo.value, o.value);
    }

    @Test
    public void test_jsonb() {
        URI[] values = new URI[]{
                uri, null
        };

        for (URI value : values) {
            VO vo = new VO();
            vo.setValue(value);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            JSONBDump.dump(jsonbBytes);

            VO v1 = JSONB.parseObject(jsonbBytes, VO.class);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    public static class VO {
        private URI value;

        public URI getValue() {
            return value;
        }

        public void setValue(URI value) {
            this.value = value;
        }
    }
}
