package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectReaderBaseModuleTest {
    @Test
    public void test() {
        ObjectReaderBaseModule.PrimitiveImpl impl = new ObjectReaderBaseModule.PrimitiveImpl() {
            @Override
            public Object readJSONBObject(JSONReader jsonReader, long features) {
                return null;
            }

            @Override
            public Object readObject(JSONReader jsonReader, long features) {
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
}
