package com.alibaba.fastjson2.issues_3600.issue3601;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplList;
import lombok.var;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FastJson2Reader {
    public static class JsonReaderModule
            implements ObjectReaderModule {
        @Override
        public ObjectReader getObjectReader(Type type) {
            if (type instanceof ParameterizedType) {
                var ptype = (ParameterizedType) type;
                if (ptype.getRawType() == MyArrayList.class) {
                    return ObjectReaderImplList.of(type, null, 0);
                }
            }
            return ObjectReaderModule.super.getObjectReader(type);
        }
    }

    public static void init() {
        JSON.register(new JsonReaderModule());
    }
}
