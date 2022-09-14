package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest0 {
    @Test
    public void test() {
        Pojo pojo = new Pojo();
        pojo.setParentClass(new ChildClass());

        byte[] jsonbBytes = JSONB.toBytes(pojo, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        JSONBDump.dump(jsonbBytes);

        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(
                true,
                Pojo.class.getName(),
                ParentClass.class.getName(),
                ChildClass.class.getName()
        );

        Pojo pojo1 = (Pojo) JSONB.parseObject(jsonbBytes, Object.class,
                filter,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.SupportClassForName,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );

        assertEquals(pojo.parentClass.getClass(), pojo1.parentClass.getClass());
    }

    private static class Pojo
            implements Serializable {
        private ParentClass parentClass;

        public ParentClass getParentClass() {
            return parentClass;
        }

        public void setParentClass(ParentClass parentClass) {
            this.parentClass = parentClass;
        }
    }

    public static class ParentClass {
    }

    public static class ChildClass
            extends ParentClass
            implements Serializable {
        private String param = "param";

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
}
