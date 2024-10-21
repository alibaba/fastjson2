package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BigIntegerTests {
    @Test
    public void test() throws Exception {
        ObjectWriter objectWriter = ObjectWriters.objectWriter(
                ObjectWriterCreator.INSTANCE.createFieldWriter("value", BigInteger.class, Bean::getValue)
        );

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = BigInteger.ONE;
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\"value\":1}", jsonWriter.toString());

            assertNotNull(objectWriter.getFieldWriter("value").getFunction());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNulls);
            Bean bean = new Bean();
            bean.value = BigInteger.ONE;
            objectWriter.write(jsonWriter, bean);
            assertEquals("{\n" +
                    "\t\"value\": 1\n" +
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
            assertEquals("{\"value\":null}", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            Bean bean = new Bean();
            bean.value = BigInteger.ONE;
            objectWriter.getFieldWriter("value").writeValue(jsonWriter, bean);
            assertEquals("1", jsonWriter.toString());
        }
    }

    public static class Bean {
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }
}
