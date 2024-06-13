package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
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
        String str = JSON.toJSONString(enumList);
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
