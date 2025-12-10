package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3864 {
    @Test
    public void testSerializationPriority() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        try {
            provider.register(Date.class, (jsonWriter, o, o1, type, l) -> jsonWriter.writeInt64(((Date) o).getTime()));

            CustomDateDo dateDo = new CustomDateDo();
            dateDo.setDate(new Date(1673861993477L));
            dateDo.setDate2(new Date(1673861993477L));

            String json = JSON.toJSONString(dateDo);
            assertEquals("{\"date\":1673861993477,\"date2\":\"2023-01-16 17:39:53\"}", json);
        } finally {
            provider.unregister(Date.class);
        }
    }

    @Data
    public static class CustomDateDo {
        private Date date;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date date2;
    }
}
