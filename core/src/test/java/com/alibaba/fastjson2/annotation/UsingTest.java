package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UsingTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"123元\"}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean {
        @JSONField(serializeUsing = IdCodec.class, deserializeUsing = IdCodec.class)
        public int id;
    }

    public static class IdCodec
            implements ObjectReader, ObjectWriter {
        public long getFeatures() {
            return 0;
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            String str = jsonReader.readString();
            str = str.replace("元", "");
            return Integer.parseInt(str);
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString(object.toString() + "元");
        }
    }
}
