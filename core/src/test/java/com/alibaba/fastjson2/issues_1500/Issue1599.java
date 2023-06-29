package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1599 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.backgroundColor = 0xFEF0FE;
        bean.borderColor = 0xEE00CC;

        String str = JSON.toJSONString(bean);
        assertEquals("{\"background-color\":\"#00FEF0FE\",\"border-color\":\"#00EE00CC\"}", str);
    }

    public class Bean {
        @JSONField(name = "background-color", format = "#%08X")
        public int backgroundColor;
        @JSONField(name = "border-color", format = "#%08X")
        public int borderColor;
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.backgroundColor = 0xFEF0FE;
        bean.borderColor = 0xEE00CC;

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, bean);
        String str = jsonWriter.toString();
        assertEquals("{\"background-color\":\"#00FEF0FE\",\"border-color\":\"#00EE00CC\"}", str);
    }
}
