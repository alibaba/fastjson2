package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("date")
public class Issue7956Test {
    static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Test
    public void testFormatYMDHMS19NoTrailingNull() {
        Date date = new Date();
        String str = DateUtils.format(date, FORMAT);

        assertEquals(19, str.length());
        assertFalse(str.indexOf('\u0000') >= 0);
        assertEquals(str, simpleDateFormatCompat(date));
    }

    @Test
    public void testFormatDatePatternVariantsNoTrailingNull() {
        Date date = new Date();

        for (String format : new String[]{
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "dd.MM.yyyy HH:mm:ss",
                "yyyyMMdd",
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "dd.MM.yyyy"
        }) {
            String str = DateUtils.format(date, format);
            assertFalse(str.indexOf('\u0000') >= 0, "format " + format);
        }
    }

    @Test
    public void testToJSONStringMapNoTrailingNull() {
        Map<String, Date> map = new LinkedHashMap<>();
        map.put("date", new Date());

        String json = JSON.toJSONString(map, FORMAT);

        assertFalse(json.indexOf('\u0000') >= 0);
        assertEquals("{\"date\":\"" + simpleDateFormatCompat(map.get("date")) + "\"}", json);
    }

    /**
     * Reference output from the standard library path, independent of fastjson2's
     * internal string creation.
     */
    static String simpleDateFormatCompat(Date date) {
        return DateTimeFormatter.ofPattern(FORMAT).format(
                ZonedDateTime.ofInstant(date.toInstant(), DateUtils.DEFAULT_ZONE_ID)
        );
    }
}
