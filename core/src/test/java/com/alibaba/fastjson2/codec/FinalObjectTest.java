package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinalObjectTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE,
//                ObjectWriterCreatorLambda.INSTANCE,
//                ObjectWriterCreatorASM.INSTANCE
        };

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Org> objectWriter
                    = creator.createObjectWriter(Org.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Org vo = new Org();
                vo.setEmp(new Emp(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[[0]]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Org vo = new Org();
                vo.setEmp(new Emp(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[[0]]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Org vo = new Org();
                vo.setEmp(new Emp(0));
                objectWriter.write(jsonWriter, vo);
                assertEquals("[[0]]",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(Org.class, "date", 0, 0, null, Org.class.getMethod("getEmp"));
            ObjectWriter<Org> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Org vo = new Org();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                Org vo = new Org();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                Org vo = new Org();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    public static class Org {
        private Emp emp;

        public Emp getEmp() {
            return emp;
        }

        public void setEmp(Emp emp) {
            this.emp = emp;
        }
    }

    public static final class Emp {
        private int id;

        public Emp() {
        }

        public Emp(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
