package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.util.DateUtils;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class ZoneId {
    public static final String OFFSET_8_ZONE_ID_NAME = "+08:00";
    public static final ZoneId OFFSET_8_ZONE_ID = ZoneId.of(OFFSET_8_ZONE_ID_NAME);

    public static TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    public static ZoneId DEFAULT_ZONE_ID = of(DEFAULT_TIME_ZONE);

    public static final String SHANGHAI_ZONE_ID_NAME = "Asia/Shanghai";
    public static final ZoneId SHANGHAI_ZONE_ID
            = SHANGHAI_ZONE_ID_NAME.equals(DEFAULT_ZONE_ID.id)
            ? DEFAULT_ZONE_ID
            : ZoneId.of(SHANGHAI_ZONE_ID_NAME);
    public static ZoneId UTC = ZoneId.of("Z");
    public final TimeZone timeZone;
    public final String id;

    private ZoneId(TimeZone timeZone) {
        this.timeZone = timeZone;
        this.id = timeZone.getID();
    }

    public int getOffsetTotalSeconds(Instant instant) {
        int offset;
        if (this == SHANGHAI_ZONE_ID || id.equals(SHANGHAI_ZONE_ID.id)) {
            offset = DateUtils.getShanghaiZoneOffsetTotalSeconds(instant.epochSecond);
        } else {
            offset = timeZone.getOffset(instant.epochSecond * 1000) / 1000;
        }
        return offset;
    }

    public int getOffsetTotalSeconds(LocalDateTime ldt) {
        int offset = timeZone.getOffset(
                GregorianCalendar.BC,
                ldt.date.year,
                ldt.date.monthValue - 1,
                ldt.date.dayOfMonth,
                1,
                ldt.time.second * 10000
        );

        return offset / 1000;
    }

    public static ZoneId of(TimeZone timeZone) {
        return new ZoneId(timeZone);
    }

    public static ZoneId systemDefault() {
        return DEFAULT_ZONE_ID;
    }

    public static ZoneId of(String tzid) {
        if (tzid.equals("Asia/Shanghai")) {
            return SHANGHAI_ZONE_ID;
        }

        char c = tzid.charAt(0);
        if (c == '+' || c == '-') {
            tzid = "GMT" + tzid;
        } else if (c == 'Z' && tzid.length() == 1) {
            tzid = "UTC";
        }

        TimeZone timeZone = TimeZone.getTimeZone(tzid);
        return ZoneId.of(timeZone);
    }
}
