package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 16/07/2017.
 */
public class Issue1319 {
    @Test
    public void test_for_issue() throws Exception {
        MyTest test = new MyTest(1, MyEnum.Test1);
        String result = JSON.toJSONString(test, JSONWriter.Feature.WriteClassName);
        System.out.println(result);
        test = JSON.parseObject(result, MyTest.class);
        System.out.println(JSON.toJSONString(test));
        assertEquals(MyEnum.Test1, test.getMyEnum());
        assertEquals(1, test.value);
    }

    @JSONType(seeAlso = {OtherEnum.class, MyEnum.class})
    interface EnumInterface {
    }

    @JSONType(typeName = "myEnum")
    enum MyEnum implements EnumInterface {
        Test1,
        Test2
    }

    @JSONType(typeName = "other")
    enum OtherEnum implements EnumInterface {
        Other
    }

    static class MyTest {
        private int value;
        private EnumInterface myEnum;

        public MyTest() {
        }

        public MyTest(int property, MyEnum enumProperty) {
            this.value = property;
            this.myEnum = enumProperty;
        }

        public int getValue() {
            return value;
        }

        public EnumInterface getMyEnum() {
            return myEnum;
        }

        public void setMyEnum(EnumInterface myEnum) {
            this.myEnum = myEnum;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
