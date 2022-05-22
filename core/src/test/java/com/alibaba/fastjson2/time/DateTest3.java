package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest3 {
    @Test
    public void test_list() {
        Bean bean = new Bean();
        bean.dates = new ArrayList<>();
        bean.dates.add(new Date(1644578127098L));

        String str = JSON.toJSONString(bean);
        assertEquals("{\"dates\":[\"022022\"]}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(1, bean1.dates.size());
        Calendar instance = Calendar.getInstance();
        instance.setTime(bean1.dates.get(0));
        assertEquals(2022, instance.get(Calendar.YEAR));
        assertEquals(1, instance.get(Calendar.MONTH));
    }

    @Test
    public void test_null() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"dates\":null}", str);
    }

    @Test
    public void test_null1() {
        Bean1 bean = new Bean1();
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"dates\":null}", str);
    }

    public static class Bean {
        @JSONField(format = "MMyyyy")
        public List<Date> dates;
    }

    private static class Bean1 {
        @JSONField(format = "MMyyyy")
        public List<Date> dates;
    }
}
