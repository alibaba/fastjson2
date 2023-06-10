package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BigIntegerField1;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigIntegerFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BigIntegerField1> objectWriter = creator.createObjectWriter(BigIntegerField1.class);

            {
                BigIntegerField1 vo = new BigIntegerField1();
                vo.id = BigInteger.valueOf(1);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":1}", jsonWriter.toString());
            }
            {
                BigIntegerField1 vo = new BigIntegerField1();
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}", jsonWriter.toString());
            }
            {
                BigIntegerField1 vo = new BigIntegerField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}", jsonWriter.toString());
            }
            {
                BigIntegerField1 vo = new BigIntegerField1();
                vo.id = BigInteger.valueOf(1);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
            {
                BigIntegerField1 vo = new BigIntegerField1();
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]", jsonWriter.toString());
            }
        }
    }
}
