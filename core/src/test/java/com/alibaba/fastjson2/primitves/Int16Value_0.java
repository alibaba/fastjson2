package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Short1;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.writer.ObjectWriters.fieldWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int16Value_0 {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Short1> objectWriter = creator.createObjectWriter(Short1.class);

            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Short1\",\"v0000\":1}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Short1\",\"v0000\":1}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000(null);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }

            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("{\n" +
                        "\t\"v0000\":1\n" +
                        "}", JSONB.toJSONString(jsonbBytes));
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 1);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[1]", JSONB.toJSONString(jsonbBytes));
            }
            {
                Short1 vo = new Short1();
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[null]", JSONB.toJSONString(jsonbBytes));
            }
        }
    }

    @Test
    public void test_arrayMapping_2000() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Short1> objectWriter = creator.createObjectWriter(Short1.class);

            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":2000}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Short1\",\"v0000\":2000}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.WriteClassName);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"@type\":\"com.alibaba.fastjson2_vo.Short1\",\"v0000\":2000}", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[2000]", jsonWriter.toString());
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("{\n" +
                        "\t\"v0000\":2000\n" +
                        "}", JSONB.toJSONString(jsonbBytes));
            }
            {
                Short1 vo = new Short1();
                vo.setV0000((short) 2000);
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals("[2000]", JSONB.toJSONString(jsonbBytes));
            }
        }
    }

    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (short) i;
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

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (short) i;
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
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(fieldWriter("value", VO::getValue));
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            VO vo = new VO();
            vo.value = (short) i;
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    public static class VO {
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }
}
