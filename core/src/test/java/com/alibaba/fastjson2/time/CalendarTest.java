package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Calendar1;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalendarTest {
    @Test
    public void testDate() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Calendar1> objectWriter = creator.createObjectWriter(Calendar1.class);

            JSONWriter jw = JSONWriter.of();
            jw.getContext().setZoneId(ZoneId.of("UTC+0"));

            Calendar1 vo = new Calendar1();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(0);
            vo.setDate(calendar);

            objectWriter.write(jw, vo);

            assertEquals("{\"date\":\"1970-01-01 00:00:00\"}", jw.toString());
        }
    }
}
