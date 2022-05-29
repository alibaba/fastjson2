package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1524 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.oldValue = new Value();

        String json = JSON.toJSONString(model, new NameFilter() {
            public String process(Object object, String name, Object value) {
                if ("oldValue".equals(name)) {
                    return "old_value";
                }
                return name;
            }
        });
        assertEquals("{\"old_value\":\"xx\"}", json);
    }

    @Test
    public void test_for_issue_1() {
        Model1 model = new Model1();
        model.oldValue = new Value();

        String json = JSON.toJSONString(model, new NameFilter() {
            public String process(Object object, String name, Object value) {
                if ("oldValue".equals(name)) {
                    return "old_value";
                }
                return name;
            }
        });
        assertEquals("{\"old_value\":\"xx\"}", json);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Model2 model = new Model2();
        model.oldValue = new Value();

        String json = JSON.toJSONString(model, new NameFilter() {
            public String process(Object object, String name, Object value) {
                if ("oldValue".equals(name)) {
                    return "old_value";
                }
                return name;
            }
        });
        assertEquals("{\"old_value\":\"xx\"}", json);
    }

    @Test
    public void test_for_issue_3() throws Exception {
        Model3 model = new Model3();
        model.oldValue = new Value();

        String json = JSON.toJSONString(model, new NameFilter() {
            public String process(Object object, String name, Object value) {
                if ("oldValue".equals(name)) {
                    return "old_value";
                }
                return name;
            }
        });
        assertEquals("{\"old_value\":\"xx\"}", json);
    }

    public static class Model {
        @JSONField(writeUsing = ValueSerializer.class)
        public Value oldValue;
    }

    public static class Model1 {
        @JSONField(writeUsing = ValueSerializer.class)
        private Value oldValue;

        public Value getOldValue() {
            return oldValue;
        }

        public void setOldValue(Value oldValue) {
            this.oldValue = oldValue;
        }
    }

    static class Model2 {
        @JSONField(writeUsing = ValueSerializer.class)
        public Value oldValue;
    }

    static class Model3 {
        @JSONField(writeUsing = ValueSerializer.class)
        private Value oldValue;

        public Value getOldValue() {
            return oldValue;
        }

        public void setOldValue(Value oldValue) {
            this.oldValue = oldValue;
        }
    }

    public static class Value {
    }

    public static class ValueSerializer
            implements ObjectWriter {
        public ValueSerializer() {
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("xx");
        }
    }
}
