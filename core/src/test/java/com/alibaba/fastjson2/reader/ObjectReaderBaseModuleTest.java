package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderBaseModuleTest {
    @Test
    public void test() {
        ObjectReaderPrimitive impl = new ObjectReaderPrimitive(Object.class) {
            @Override
            public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                return null;
            }

            @Override
            public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                return null;
            }
        };
        assertThrows(JSONException.class, () -> impl.createInstance(0));
        assertNull(impl.getFieldReader(0L));
    }

    @Test
    public void test1() {
        String str = "{\"@type\":\"com.alibaba.fastjson2.reader.ObjectReaderBaseModuleTest$Cat\",\"id\":123}";
        Cat cat = (Cat) JSON.parseObject(str, Animal.class, JSONReader.Feature.SupportAutoType);
        assertEquals(123, cat.id);
    }

    @JSONType(seeAlso = {})
    public interface Animal {
    }

    public static class Cat
            implements Animal {
        public int id;
    }

    @Test
    public void test2() throws Exception {
        ObjectReaderModule module = new ObjectReaderModule() {
        };

        BeanInfo beanInfo = new BeanInfo();
        module.getBeanInfo(beanInfo, Animal.class);

        FieldInfo fieldInfo = new FieldInfo();
        module.getFieldInfo(fieldInfo, Cat.class, Cat.class.getField("id"));
    }
}
