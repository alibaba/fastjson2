package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class Issue1031 {
    @Test
    public void fastjsonTest() {
        TestBean testBean = new TestBean();
        testBean.setTestEnum(TestEnum.ENUM_ONE);
        String testBeanString = JSON.toJSONString(testBean);
        System.out.println(testBeanString);

        //正常
        TestBean testBeanFromString = JSON.parseObject(testBeanString, TestBean.class);

        JSONObject testBeanJSON = JSON.parseObject(testBeanString);

        //异常
        TestBean testBeanFromJSONObject = testBeanJSON.toJavaObject(TestBean.class);
    }

    static class TestBean {
        @JSONField(serializeUsing = TestEnum.Writer.class, deserializeUsing = TestEnum.Reader.class)
        private TestEnum testEnum;

        public TestEnum getTestEnum() {
            return testEnum;
        }

        public void setTestEnum(TestEnum testEnum) {
            this.testEnum = testEnum;
        }
    }

    enum TestEnum {
        ENUM_ONE("枚举1", 3),
        ENUM_TWO("枚举2", 2),
        ENUM_THREE("枚举3", 1);
        private String name;
        private Integer value;

        TestEnum(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }

        public static TestEnum ofValue(Integer value) {
            for (TestEnum enumConstant : TestEnum.class.getEnumConstants()) {
                if (enumConstant.getValue().equals(value)) {
                    return enumConstant;
                }
            }
            return null;
        }

        public static class Writer
                implements ObjectWriter<TestEnum> {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                TestEnum testEnum = (TestEnum) object;
                jsonWriter.writeInt32(testEnum.getValue());
            }
        }

        public static class Reader
                implements ObjectReader<TestEnum> {
            @Override
            public TestEnum readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                Integer value = jsonReader.readInt32();
                return TestEnum.ofValue(value);
            }
        }
    }
}
