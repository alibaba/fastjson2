package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectArrayTest {
    @Test
    public void test() throws Exception {
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter(Bean.class, "values", null, Bean.class.getMethod("getValues"))
        );

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"values\":[1,10]}", jsonWriter.toString());

            assertNull(objectWriter.getFieldWriter("values").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\n" +
                    "\t\"values\":[\n" +
                    "\t\t1,\n" +
                    "\t\t10\n" +
                    "\t]\n" +
                    "}", JSONB.toJSONString(jsonWriter.getBytes()));
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{}", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"values\":null}", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.getFieldWriter("values").writeValue(jsonWriter, bean);
            assertEquals("[1,10]", jsonWriter.toString());
        }
    }

    private static class Bean {
        private BigDecimal[] values;

        public BigDecimal[] getValues() {
            return values;
        }

        public void setValues(BigDecimal[] values) {
            this.values = values;
        }
    }

    @Test
    public void test1() throws Exception {
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter("values", null, Bean1.class.getField("values"))
        );

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean1 bean = new Bean1();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"values\":[1,10]}", jsonWriter.toString());

            assertNull(objectWriter.getFieldWriter("values").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean1 bean = new Bean1();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\n" +
                    "\t\"values\":[\n" +
                    "\t\t1,\n" +
                    "\t\t10\n" +
                    "\t]\n" +
                    "}", JSONB.toJSONString(jsonWriter.getBytes()));
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean1 bean = new Bean1();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{}", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
            Bean1 bean = new Bean1();
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"values\":null}", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean1 bean = new Bean1();
            bean.values = new BigDecimal[] {BigDecimal.ONE, BigDecimal.TEN};
            objectWriter.getFieldWriter("values").writeValue(jsonWriter, bean);
            assertEquals("[1,10]", jsonWriter.toString());
        }
    }

    private static class Bean1 {
        public BigDecimal[] values;
    }
}
