package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2347 {
    String expected = "{\"name\":\"测试\",\"value\":null}";

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setName("测试");
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
        assertEquals(expected, str);

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
        objectWriter.write(jsonWriter, bean);
        assertEquals(expected, jsonWriter.toString());
    }

    @Getter
    @Setter
    public static class Bean {
        private String name;

        @JSONField(jsonDirect = true)
        private String value;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.setName("测试");
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
        assertEquals(expected, str);

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean1.class);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
        objectWriter.write(jsonWriter, bean);
        assertEquals(expected, jsonWriter.toString());
    }

    public static class Bean1 {
        private String name;

        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.name = "测试";
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
        assertEquals(expected, str);

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean2.class);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteNulls);
        objectWriter.write(jsonWriter, bean);
        assertEquals(expected, jsonWriter.toString());
    }

    public static class Bean2 {
        public String name;
        public String value;
    }
}
