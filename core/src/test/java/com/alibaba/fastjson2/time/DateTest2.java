package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Date1;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest2 {
    @Test
    public void testDate() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Date1> objectWriter = creator.createObjectWriter(Date1.class);

            JSONWriter jw = JSONWriter.of();
            jw.getContext().setZoneId(ZoneId.of("UTC+0"));

            Date1 vo = new Date1();
            vo.setDate(new java.util.Date(0));

            objectWriter.write(jw, vo);

            assertEquals("{\"date\":\"1970-01-01 00:00:00\"}", jw.toString());
        }
    }
}
