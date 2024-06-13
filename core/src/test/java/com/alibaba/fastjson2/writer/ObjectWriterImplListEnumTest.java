package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ObjectWriterImplListEnumTest {
    @Test
    public void testNullEnumObjInList() {
        Bean bean = new Bean();
        List<TestEnum> enumList = new ArrayList<>();
        enumList.add(TestEnum.CN);
        enumList.add(null);
        enumList.add(TestEnum.EN);
        bean.setEnumList(enumList);
        ObjectWriterImplListEnum writerImplListEnum = new ObjectWriterImplListEnum(Bean.class, TestEnum.class,0L);
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        final String fieldName = "enumList";
        writerImplListEnum.writeJSONB(jsonWriter, enumList, fieldName, TestEnum.class, 0L);
    }

    static class Bean {
        private List<TestEnum> enumList;

        public List<TestEnum> getEnumList() {
            return enumList;
        }

        public void setEnumList(List<TestEnum> enumList) {
            this.enumList = enumList;
        }
    }

    enum TestEnum {
        EN,
        CN;
    }
}
