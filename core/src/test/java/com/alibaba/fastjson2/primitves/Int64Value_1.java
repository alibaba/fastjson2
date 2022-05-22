package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int64Value_1 {
    private long[] values = new long[100];
    private int off;

    public Int64Value_1() {
        values[off++] = Byte.MIN_VALUE;
        values[off++] = Byte.MAX_VALUE;
        values[off++] = Short.MIN_VALUE;
        values[off++] = Short.MAX_VALUE;
        values[off++] = Integer.MIN_VALUE;
        values[off++] = Integer.MAX_VALUE;
        values[off++] = Long.MIN_VALUE;
        values[off++] = Long.MAX_VALUE;
        values[off++] = 0;
        values[off++] = -1;
        values[off++] = -10;
        values[off++] = -100;
        values[off++] = -1000;
        values[off++] = -10000;
        values[off++] = -100000;
        values[off++] = -1000000;
        values[off++] = -10000000;
        values[off++] = -100000000;
        values[off++] = -1000000000;
        values[off++] = -10000000000L;
        values[off++] = -100000000000L;
        values[off++] = -1000000000000L;
        values[off++] = -10000000000000L;
        values[off++] = -100000000000000L;
        values[off++] = -1000000000000000L;
        values[off++] = -10000000000000000L;
        values[off++] = -100000000000000000L;
        values[off++] = -1000000000000000000L;
        values[off++] = 1;
        values[off++] = 10;
        values[off++] = 100;
        values[off++] = 1000;
        values[off++] = 10000;
        values[off++] = 100000;
        values[off++] = 1000000;
        values[off++] = 10000000;
        values[off++] = 100000000;
        values[off++] = 1000000000;
        values[off++] = 10000000000L;
        values[off++] = 100000000000L;
        values[off++] = 1000000000000L;
        values[off++] = 10000000000000L;
        values[off++] = 100000000000000L;
        values[off++] = 1000000000000000L;
        values[off++] = 10000000000000000L;
        values[off++] = 100000000000000000L;
        values[off++] = 1000000000000000000L;
        values[off++] = -9;
        values[off++] = -99;
        values[off++] = -999;
        values[off++] = -9999;
        values[off++] = -99999;
        values[off++] = -999999;
        values[off++] = -9999999;
        values[off++] = -99999999;
        values[off++] = -999999999;
        values[off++] = -9999999999L;
        values[off++] = -99999999999L;
        values[off++] = -999999999999L;
        values[off++] = -9999999999999L;
        values[off++] = -99999999999999L;
        values[off++] = -999999999999999L;
        values[off++] = -9999999999999999L;
        values[off++] = -99999999999999999L;
        values[off++] = -999999999999999999L;
        values[off++] = 9;
        values[off++] = 99;
        values[off++] = 999;
        values[off++] = 9999;
        values[off++] = 99999;
        values[off++] = 999999;
        values[off++] = 9999999;
        values[off++] = 99999999;
        values[off++] = 999999999;
        values[off++] = 9999999999L;
        values[off++] = 99999999999L;
        values[off++] = 999999999999L;
        values[off++] = 9999999999999L;
        values[off++] = 99999999999999L;
        values[off++] = 999999999999999L;
        values[off++] = 9999999999999999L;
        values[off++] = 99999999999999999L;
        values[off++] = 999999999999999999L;
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
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("value", VO::getValue));
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
    public void test_asm() throws Exception {
        for (int i = 0; i < off; i++) {
            VO vo = new VO();
            vo.value = values[i];
            JSONWriter w = JSONWriter.of();
            w.writeAny(vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = jr.read(VO.class);
            assertEquals(vo.value, o.value);
        }
    }

    public static class VO {
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();
        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        for (int i = 0; i < creators.length; i++) {
            ObjectReaderCreator creator = creators[i];

            VO vo = new VO();

            JSONPath jsonPath = JSONPath
                    .of("$.value")
                    .setReaderContext(
                            new JSONReader.Context(
                                    new ObjectReaderProvider(creator))
                    )
                    .setWriterContext(
                            new JSONWriter.Context(
                                    new ObjectWriterProvider(writerCreators[i]))
                    );
            jsonPath.set(vo, 101);
            assertEquals(101, vo.getValue());
            jsonPath.set(vo, 102L);
            assertEquals(102, vo.getValue());
            jsonPath.set(vo, null);
            assertEquals(0, vo.getValue());
            jsonPath.set(vo, "103");
            assertEquals(103, vo.getValue());
            assertEquals(103L, jsonPath.eval(vo));

            jsonPath.setInt(vo, 101);
            assertEquals(101, vo.getValue());
            jsonPath.setLong(vo, 102L);
            assertEquals(102, vo.getValue());
        }
    }
}
