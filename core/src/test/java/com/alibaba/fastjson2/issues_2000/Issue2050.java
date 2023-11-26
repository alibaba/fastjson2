package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2050 {
    static class TestClass {

        private Integer field1 = 1;
        private Integer field2;
        private Integer field3 = 3;

        public void setField1(Integer field1) {
            if (field1 == null) throw new NullPointerException();
            this.field1 = field1;
        }

        public void setField2(Integer field2) {
            if (field2 == null) throw new NullPointerException();
            this.field2 = field2;
        }

        public void setField3(Integer field3) {
            this.field3 = field3;
        }

        public Integer getField1() {
            return field1;
        }

        public Integer getField2() {
            return field2;
        }

        public Integer getField3() {
            return field3;
        }
    }

    @Test
    void test() {

        // Assertion 1
        // Deserialize json string which not contains null field
        // Passed.
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                JSONObject.parseObject("{\"field1\": -1}", TestClass.class);
                JSONObject.parseObject("{\"field2\": 2}", TestClass.class);
                JSONObject.parseObject("{}", TestClass.class);
            }
        });

        // Assertion 2
        // Deserialize json string which contains null field without JSONReader.Feature.IgnoreSetNullValue
        // Passed, because it called the `setField2` setter and threw an NPE
        assertThrows(Exception.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                JSONObject.parseObject("{\"field1\": -1, \"field2\": null}", TestClass.class);
            }
        });

        // Assertion 3
        // Deserialize json string which contains null field without JSONReader.Feature.IgnoreSetNullValue,
        // but using `to` method.
        // Failed. It didn't call the `setField2` setter, so there's no any experted NPE presented.
        // Why? I didn't set neither IgnoreSetNullValue feature nor IgnoreNullPropertyValue feature,
        // shouldn't the `JSONObject.parseObject(String, Class<T>)` shares the same logic with `JSONObject.parseObject(String).to(Class<T>)`?
        assertThrows(Exception.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                // Addictionally, field1 is set to -1 successfully, but seems it automatically ignores field2
                // without appointed any ignored feature.
                JSONObject.parseObject("{\"field1\": -1, \"field2\": null}").to(TestClass.class);
            }
        });

        // Assertion 4
        // And go back to my original purpose, deserialize json string which contains null field with JSONReader.Feature.IgnoreSetNullValue
        // Failed, JSONReader.Feature.IgnoreSetNullValue didn't work.
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                JSONObject.parseObject("{\"field1\": null}", TestClass.class, JSONReader.Feature.IgnoreSetNullValue);
            }
        });

        // Assertion 5
        // Also failed, JSONReader.Feature.IgnoreNullPropertyValue didn't work.
        // But maybe I've confused the difference between `IgnoreNullPropertyValue` and `IgnoreSetNullValue`
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                JSONObject.parseObject("{\"field1\": null}", TestClass.class, JSONReader.Feature.IgnoreNullPropertyValue);
            }
        });

        // Go further, try setting a null value to a default-init not null field: field3.
        // Neither IgnoreSetNullValue feature nor IgnoreNullPropertyValue feature is set.
        TestClass testOverrideDefaultValue = JSONObject.parseObject("{\"field1\": -1,\"field3\": null}").to(TestClass.class);

        // Assertion 6. Passed.
        assertEquals(-1, testOverrideDefaultValue.getField1());

        // Assertion 7. Failed, also because the `setField3` setter is not called. It's acturally a same issue with assertion 3
        assertTrue(null == testOverrideDefaultValue.getField3());

        // As comparing, using `JSONObject.parseObject(String, Class<T>)` method:
        TestClass testOverrideDefaultValue2 = JSONObject.parseObject("{\"field1\": -1,\"field3\": null}", TestClass.class);

        // Assertion 8. Passed.
        assertEquals(-1, testOverrideDefaultValue2.getField1());

        // Assertion 9. Passed.
        assertTrue(null == testOverrideDefaultValue2.getField3());

    }
}
