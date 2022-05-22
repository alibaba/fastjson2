package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONType_serializer {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        assertEquals("{\"ID\":123}", JSON.toJSONString(bean));
    }

    @JSONType(serializer = BeanWriter.class)
    public static class Bean {
        public int id;
    }

    public static class BeanWriter
            implements ObjectWriter<Bean> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Bean bean = (Bean) object;

            jsonWriter.startObject();

            jsonWriter.writeName("ID");
            jsonWriter.writeColon();
            jsonWriter.writeInt32(bean.id);

            jsonWriter.endObject();
        }
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.id = 123;

        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            ObjectWriter objectWriter = creator.createObjectWriter(Bean.class);
            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, bean);
            String str = jsonWriter.toString();
            assertEquals("{\"ID\":123}", str);
        }
    }
}
