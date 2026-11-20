package com.alibaba.fastjson2.issues_3200;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3297 {
    @Test
    public void testGlobalFormatRoundTrip() {
        String readerFormat = JSONFactory.createReadContext().getDateFormat();
        String writerFormat = JSONFactory.createWriteContext().getDateFormat();
        try {
            JSON.configReaderDateFormat("yyyy-MM-dd HH:mm:ss");
            JSON.configWriterDateFormat("yyyy-MM-dd HH:mm:ss");
            Bean input = new Bean();
            input.date3 = new DateTime(1737378009000L);
            Bean output = JSON.parseObject(JSON.toJSONString(input), Bean.class);
            assertEquals(input.date3.getTime(), output.date3.getTime());
        } finally {
            JSON.configReaderDateFormat(readerFormat);
            JSON.configWriterDateFormat(writerFormat);
        }
    }

    @Test
    public void testDateTimeField() {
        Bean bean = JSON.parseObject("{\"date3\":\"2025-01-20 13:00:09\"}", Bean.class,
                "yyyy-MM-dd HH:mm:ss");
        assertEquals(DateTime.class, bean.date3.getClass());
        assertEquals(JSON.parseObject("\"2025-01-20 13:00:09\"", Date.class,
                "yyyy-MM-dd HH:mm:ss").getTime(), bean.date3.getTime());
    }

    @Test
    public void testInputs() {
        long millis = 1737378009000L;
        String text = "{\"date3\":" + millis + "}";
        assertEquals(millis, JSON.parseObject(text, Bean.class).date3.getTime());
        assertEquals(millis, JSON.parseObject(text.getBytes(StandardCharsets.UTF_8), Bean.class).date3.getTime());
        assertEquals(millis, JSON.parseObject(text, Bean.class, JSONReader.Feature.FieldBased).date3.getTime());
        assertEquals(millis, JSON.parseObject(Long.toString(millis), DateTime.class).getTime());
        assertNull(JSON.parseObject("null", DateTime.class));
        assertNull(JSON.parseObject("{\"date3\":null}", Bean.class).date3);
        Bean bean = new Bean();
        bean.date3 = new DateTime(millis);
        assertEquals(millis, JSONB.parseObject(JSONB.toBytes(bean), Bean.class).date3.getTime());
    }

    @Test
    public void testFieldFormat() {
        FormattedBean bean = JSON.parseObject("{\"date\":\"20/01/2025\"}", FormattedBean.class);
        assertEquals(JSON.parseObject("\"20/01/2025\"", Date.class, "dd/MM/yyyy").getTime(), bean.date.getTime());
    }

    public static class Bean {
        public DateTime date3;
    }

    public static class FormattedBean {
        @JSONField(format = "dd/MM/yyyy")
        public DateTime date;
    }
}
