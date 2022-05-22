package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BooleanValueField1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanValueFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BooleanValueField1> objectWriter = creator.createObjectWriter(BooleanValueField1.class);

            {
                BooleanValueField1 vo = new BooleanValueField1();
                vo.v0000 = true;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":true}", jsonWriter.toString());
            }
            {
                BooleanValueField1 vo = new BooleanValueField1();
                vo.v0000 = false;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":false}", jsonWriter.toString());
            }
            {
                BooleanValueField1 vo = new BooleanValueField1();
                vo.v0000 = true;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[true]", jsonWriter.toString());
            }
            {
                BooleanValueField1 vo = new BooleanValueField1();
                vo.v0000 = false;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[false]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<BooleanValueField1> objectWriter = creator.createObjectReader(BooleanValueField1.class);
            {
                BooleanValueField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":1}"), 0);
                assertEquals(true, vo.v0000);
            }
            {
                BooleanValueField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":false}"), 0);
                assertEquals(false, vo.v0000);
            }
            {
                BooleanValueField1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":\"true\"}"), 0);
                assertEquals(true, vo.v0000);
            }
        }
    }

    @Test
    public void test_read_1() {
        A a = new A();
        a.value = true;

        String str = JSON.toJSONString(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"value\":true}", str);

        A a1 = JSON.parseObject(str, A.class, JSONReader.Feature.FieldBased);
        assertEquals(a.value, a1.value);
    }

    @Test
    public void test_read_2() {
        B b = new B();
        b.value = true;

        String str = JSON.toJSONString(b, JSONWriter.Feature.FieldBased);
        assertEquals("{\"value\":true}", str);

        B b1 = JSON.parseObject(str, B.class, JSONReader.Feature.FieldBased);
        assertEquals(b.value, b1.value);
    }

    static class A {
        boolean value;
    }

    static class B
            extends A {
    }
}
