package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateWriterReviewTest {
    @Test
    public void dateFieldUsesDeclaredLocale() {
        Bean bean = new Bean();
        bean.date = new Date(0);
        ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        context.setZoneId(ZoneOffset.UTC);
        String date = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)
                .format(Instant.EPOCH.atZone(ZoneOffset.UTC));
        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.writeAny(bean);
            assertEquals("{\"date\":\"" + date + "\"}", writer.toString());
        }
    }

    @Test
    public void dateOutsideFourDigitYearUsesFallback() {
        ZonedDateTime dateTime = ZonedDateTime.of(10000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setZoneId(ZoneOffset.UTC);
        try (JSONWriter writer = JSONWriter.of(context);
                JSONWriter expected = JSONWriter.of(context)) {
            expected.writeZonedDateTime(dateTime);
            writer.writeAny(Date.from(dateTime.toInstant()));
            assertEquals(expected.toString(), writer.toString());
        }
    }

    public static class Bean {
        @JSONField(format = "dd MMMM yyyy", locale = "fr_FR")
        public Date date;
    }
}
