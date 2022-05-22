package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListStr_0 {
    Type[] types = Type.class.getEnumConstants();

    @Test
    public void test_asm() throws Exception {
        ObjectWriter<VO> ow = TestUtils.WRITER_CREATOR.createObjectWriter(VO.class);
        ObjectReader<VO> oc = TestUtils.READER_CREATOR.createObjectReader(VO.class);

        for (Type type : types) {
            VO vo = new VO();
            vo.value = Arrays.asList("a", "b", "c");
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            System.out.println(json);
//            JSONReader jr = JSONReader.of(json);
//            VO o = oc.deserialize(jr, null);
//            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_jsonb() throws Exception {
        ObjectWriter<VO> ow = TestUtils.WRITER_CREATOR.createObjectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaderCreator.INSTANCE.createObjectReader(VO.class);

        for (Type type : types) {
            VO vo = new VO();
            vo.value = Arrays.asList("a", "b", "c");
            JSONWriter w = JSONWriter.ofJSONB();
            ow.write(w, vo);

            byte[] jsonb = w.getBytes();
            JSONReader jr = JSONReader.ofJSONB(jsonb, 0, jsonb.length);
            VO o = jr.read(VO.class);
            assertEquals(vo.value, o.value);
        }
    }

    public static class VO {
        private List<String> value;

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }

    public static enum Type {
        T0,
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
    }
}
