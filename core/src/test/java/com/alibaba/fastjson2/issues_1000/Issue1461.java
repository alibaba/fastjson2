package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1461 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.value = true;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"Y\"}", str);
    }

    public static class Bean {
        @JSONField(serializeUsing = Bool2Yn.class)
        public Boolean value;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        String str = JSON.toJSONString(bean);
        assertEquals("{\"validYn\":\"Y\"}", str);
    }

    public static class Bean1 {
        @JSONField(serializeUsing = Bool2Yn.class)
        public Boolean validYn() {
            return true;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.value = true;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"Y\"}", str);
    }

    @Data
    public static class Bean2 {
        @JSONField(serializeUsing = Bool2Yn.class)
        private Boolean value;
    }

    public static class Bool2Yn
            implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Boolean value = (Boolean) object;
            jsonWriter.writeString(value ? "Y" : "N");
        }
    }
}
