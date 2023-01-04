package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue413 {
    @Test
    public void test() {
        String date1 = JSONObject.of("date1", "1654134285").toString();
        Bean bean = JSON.parseObject(date1, Bean.class);
        assertEquals(1654134285000L, bean.getDate1().getTime());

        JSONPath.set(bean, "$.date1", "1654134286");
        assertEquals(1654134286000L, bean.getDate1().getTime());

        long millis = 1654134286001L;
        JSONPath.set(bean, "$.date1", new Date(millis));
        assertEquals(1654134286001L, bean.getDate1().getTime());
    }

    @Test
    public void testLambda() {
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        String date1 = JSONObject.of("date1", "1654134285").toString();
        Bean bean = objectReader.readObject(JSONReader.of(date1));
        assertEquals(1654134285000L, bean.getDate1().getTime());
        objectReader.getFieldReader("date1")
                .accept(bean, "1654134286");
        assertEquals(1654134286000L, bean.getDate1().getTime());
    }

    @Data
    public static class Bean {
        @JSONField(format = "unixtime")
        private Date date1;
    }

    @Test
    public void test1() {
        String date1 = JSONObject.of("date1", "1654134285").toString();
        Bean1 bean = JSON.parseObject(date1, Bean1.class);
        assertEquals(1654134285000L, bean.date1.getTime());

        JSONPath.set(bean, "$.date1", "1654134286");
        assertEquals(1654134286000L, bean.date1.getTime());

        long millis = 1654134286001L;
        JSONPath.set(bean, "$.date1", new Date(millis));
        assertEquals(1654134286001L, bean.date1.getTime());
    }

    public static class Bean1 {
        @JSONField(format = "unixtime")
        public Date date1;
    }
}
