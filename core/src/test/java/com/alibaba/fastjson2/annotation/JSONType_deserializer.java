package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONType_deserializer {
    @Test
    public void test() {
        String str = "{\"beanId\":123}";
        Bean bean = JSON.parseObject(str, Bean.class);

        assertEquals(123, bean.id);
    }

    @JSONType(deserializer = BeanReader.class)
    public static class Bean {
        public int id;
    }

    public static class BeanReader
            implements ObjectReader<Bean> {
        @Override
        public Bean readObject(JSONReader jsonReader, Type fieldType, Object fieldName0, long features) {
            Bean bean = new Bean();
            jsonReader.nextIfObjectStart();
            for (; ; ) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }
                String fieldName = jsonReader.readFieldName();
                if (fieldName.equals("beanId")) {
                    bean.id = jsonReader.readInt32Value();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.nextIfMatch(',');
            return bean;
        }
    }

    @Test
    public void test1() {
        String str = "{\"beanId\":123}";

        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Bean> objectReader = creator.createObjectReader(Bean.class);

            JSONReader jsonReader = JSONReader.of(str);
            Bean bean = objectReader.readObject(jsonReader);

            assertEquals(123, bean.id);
        }
    }
}
