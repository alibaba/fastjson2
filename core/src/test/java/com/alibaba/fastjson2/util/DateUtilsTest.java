package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static com.alibaba.fastjson2.util.IOUtils.DEFAULT_ZONE_ID;
import static com.alibaba.fastjson2.util.IOUtils.SHANGHAI_ZONE_ID;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {
    @Test
    public void parseDateNullOrEmpty() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
        assertNull(DateUtils.parseDate("null"));
    }

    @Test
    public void parseDate0() {
        String str = "2022-04-29 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate1() {
        String str = "2022-04-29";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate3() {
        String str = "2022-4-29";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-dd");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate4() {
        long millis = System.currentTimeMillis();
        String str = Long.toString(millis);
        assertEquals(
                millis,
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate5() {
        String str = "2022-04-29 12:13:14Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str.substring(0, 19), fmt);
        assertEquals(
                ldt.atZone(UTC).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate6() {
        String str = "2022-04-29 12:13Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime ldt = LocalDateTime.parse(str.substring(0, 16), fmt);
        assertEquals(
                ldt.atZone(UTC).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate7() {
        String str = "2022-11-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate8() {
        String str = "2022-1-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate9() {
        String str = "2022/04/29 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L8() {
        String str = "20220203";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 2, 3), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID)
                        .toInstant()
                        .toEpochMilli(),
                DateUtils.parseDate(str)
                        .getTime()
        );
    }

    @Test
    public void parseDate_L9() {
        String str = "2022년1월2일";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 1, 2), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID)
                        .toInstant()
                        .toEpochMilli(),
                DateUtils.parseDate(str)
                        .getTime()
        );
    }

    @Test
    public void parseDate_L9_1() {
        String str = "2022-11-2";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 11, 2), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID)
                        .toInstant()
                        .toEpochMilli(),
                DateUtils.parseDate(str)
                        .getTime()
        );
    }

    @Test
    public void parseDate_L9_2() {
        String str = "2022-1-12";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 1, 12), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID)
                        .toInstant()
                        .toEpochMilli(),
                DateUtils.parseDate(str)
                        .getTime()
        );
    }

    @Test
    public void parseDate_L9_3() {
        String str = "2022/12/1";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/M/d");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L9_4() {
        String str = "2022/1/21";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/M/d");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L9_5() {
        String str = "1.12.2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L9_6() {
        String str = "12.1.2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L9_7() {
        String str = "1-12-2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d-M-yyyy");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L9_8() {
        String str = "12-1-2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d-M-yyyy");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L10() {
        String str = "2022년11월12일";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 11, 12), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L10_1() {
        String str = "2021-02-02";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2021, 2, 2), LocalTime.MIN);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L18_0() {
        String str = "2021-02-02 10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 2, 2, 10, 12, 34);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDate_L19_0() {
        String str = "2021-02-02  10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 2, 2, 10, 12, 34);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDateZ() {
        String str = "2022-04-29T00:00:00Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str.substring(0, 19), fmt);
        ZoneId zoneId = UTC;
        assertEquals(
                ldt.atZone(zoneId).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDateZ1() {
        String str = "2022-04-29T00:00:00+08:00";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str.substring(0, 19), fmt);
        ZoneId zoneId = ZoneId.of("GMT+8");
        assertEquals(
                ldt.atZone(zoneId).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseDateZ2() {
        String str = "2022-04-29T00:00:00[Asia/Shanghai]";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str.substring(0, 19), fmt);
        assertEquals(
                ldt.atZone(SHANGHAI_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str).getTime()
        );
    }

    @Test
    public void parseLocalDate_L8() {
        String str = "20220112";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(str, fmt);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
    }

    @Test
    public void parseLocalDate_L8_1() {
        String str = "2022-1-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate = LocalDate.parse(str, fmt);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
    }

    @Test
    public void parseLocalDate_NullOrError() {
        assertNull(DateUtils.parseLocalDate(null));
        assertNull(DateUtils.parseLocalDate(null, 0, 0));
        assertNull(DateUtils.parseLocalDate(""));
        assertNull(DateUtils.parseLocalDate("null"));
        assertNull(DateUtils.parseLocalDate("00000000"));
    }

    @Test
    public void parseLocalDate_L8_NullOrError() {
        assertNull(DateUtils.parseLocalDate("00000000"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate8("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("A0000000"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000A000"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("20001300"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("200001A0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("20000100"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("20000140"));
    }

    @Test
    public void parseLocalDate_L9_NullOrError() {
        assertNull(DateUtils.parseLocalDate("0000-0-00"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate9("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("A000-0-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-A-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-1-40"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-13-1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000A13-1"));
    }

    @Test
    public void parseLocalDate_L10() {
        String str = "2022-01-12";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(str, fmt);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
    }

    @Test
    public void parseLocalDate_L10_NullOrError() {
        assertNull(DateUtils.parseLocalDate("0000-00-00"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate10("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000A00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("A000-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2A00-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("20A0-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("200A-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("$000-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2$00-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("20$0-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("200$-00-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("A000/00/00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("00.00.A000"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("00-00-A000"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-0A-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-A0-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-0$-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-$0-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-40"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-0A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-A0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-0$"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-$0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-01-00"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000-13-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000A13-01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年1月40日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年13月1日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000년1월40일"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000년13월1일"));
    }

    @Test
    public void parseLocalDate_L11_NullOrError() {
        assertNull(DateUtils.parseLocalDate("0000年00月00日"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate11("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("A000年00月00日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年00月00日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年20月00日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年A0月00日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年0A月00日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年10月40日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年10月A0日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年10月0A日"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000年10月40A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDate("2000년00월00일"));
    }

    @Test
    public void parseLocalDateTime_0() {
        String str = "2022-1-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(str, fmt), LocalTime.MIN);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str)
        );
    }

    @Test
    public void parseLocalDateTime_1() {
        String str = "2022-01-02 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str)
        );
    }

    @Test
    public void parseLocalDateTime_L14() {
        String str = "20220102121314";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str)
        );
    }

    @Test
    public void parseLocalDateTime_L12() {
        String str = "202201021213";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str)
        );
    }

    @Test
    public void parseLocalDateTime_NullOrError() {
        assertNull(DateUtils.parseLocalDateTime(null));
        assertNull(DateUtils.parseLocalDateTime(null, 0, 0));
        assertNull(DateUtils.parseLocalDateTime(""));
        assertNull(DateUtils.parseLocalDateTime("null"));
        assertNull(DateUtils.parseLocalDateTime("000000000000"));
        assertNull(DateUtils.parseLocalDateTime("00000000"));
        assertNull(DateUtils.parseLocalDateTime("0000-00-0"));
        assertNull(DateUtils.parseLocalDateTime("0000-00-00"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("xxxx"));
    }

    @Test
    public void parseLocalDateTime_L12_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime12("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("202A01021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20A001021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2A0001021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A00001021213"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$00001021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2$0001021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20$001021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200$01021213"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000A0021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000A021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000$0021213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000$021213"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010A1213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001A01213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010$1213"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001$01213"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101A013"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010A13"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101$013"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010$13"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101010A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101A0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101010$"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101$0"));
    }

    @Test
    public void parseLocalDateTime_L14_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime14("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A0000101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("0A000101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("00A00101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("000A0101010101"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$0000101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("0$000101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("00$00101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("000$0101010101"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000A101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000A01010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000$101010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000$01010101"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001A1010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010A010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001$1010101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010$010101"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101A00101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010A0101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101$00101"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010$0101"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101A001"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101010A01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101$001"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101010$01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010101A0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101010A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001010101$0"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010101010$"));
    }

    @Test
    public void parseLocalDateTime_L16_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime16("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101X010101Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A0000101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("0A000101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("00A00101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("000A0101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$0000101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("0$000101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("00$00101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("000$0101T010101Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000A101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000A01T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000$101T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000$01T010101Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001A1T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010AT010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200001$1T010101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000010$T010101Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101TA00101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T0A0101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T$00101Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T0$0101Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T01A001Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T010A01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T01$001Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T010$01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T0101A0Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T01010AZ"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T0101$0Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20000101T01010$Z"));
    }

    @Test
    public void parseLocalDateTime_L17_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime17("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:01X"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A000-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2A00-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20A0-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200A-01-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$000-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2$00-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20$0-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200$-01-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-A1-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0A-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-$1-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0$-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-A1T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0AT01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-$1T01:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0$T01:01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01TA1:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0A:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T$1:01Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0$:01Z"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:A1Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0AZ"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:$1Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0$Z"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-1 01:01:A1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-1 01:01:0A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-1 01:01:$1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-1 01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_L18_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime18("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01X01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01A01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A000-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2A00-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20A0-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200A-1-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$000-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2$00-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20$0-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200$-1-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-A1-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0A-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-$1-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0$-1T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-A1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-0AT01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-$1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-0$T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01TA1:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T0A:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T$1:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T0$:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T01:A1:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T01:0A:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T01:$1:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01T01:0$:01"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01 01:01:A1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01 01:01:0A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01 01:01:$1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-1-01 01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_L19_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime19("00", 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01A01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A000-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2A00-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20A0-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200A-01-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$000-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2$00-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20$0-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200$-01-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-A1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0A-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-$1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0$-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-A1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0AT01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-$1T01:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0$T01:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01TA1:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0A:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T$1:01:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0$:01:01"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:A1:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0A:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:$1:01"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0$:01"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:A1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:0A"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:$1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:0$"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000/01/01 01:01:0$"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000/01/01T01:01:0$"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("01/01/2000 01:01:0$"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("01/01/2000T01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_X_NullOrError() {
        assertNull(DateUtils.parseLocalDateTimeX(null, 0, 0));
        assertNull(DateUtils.parseLocalDateTimeX("", 0, 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTimeX("00", 0, 1));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.12"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.1234"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.12345"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.123456"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.1234567"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.12345678"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01X01:01:01.123456789"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01A01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("A000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2A00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20A0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200A-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("$000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2$00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("20$0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("200$-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-A1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0A-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-$1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-0$-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-A1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0AT01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-$1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-0$T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01TA1:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0A:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T$1:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T0$:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:A1:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0A:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:$1:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01T01:0$:01.123"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:A1.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:0A.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:$1.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000-01-01 01:01:0$.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000/01/01 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("2000/01/01T01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("01/01/2000 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime("01/01/2000T01:01:0$.123"));
    }

    @Test
    public void parseZonedDateTime_X_NullOrError() {
        assertNull(DateUtils.parseZonedDateTime(null, 0, 0));
        assertNull(DateUtils.parseZonedDateTime("", 0, 0));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("00", 0, 1));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.1"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.12"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.1234"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.12345"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.123456"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.1234567"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.12345678"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01X01:01:01.123456789"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01A01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("A000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2A00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("20A0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("200A-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("$000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2$00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("20$0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("200$-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-A1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-0A-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-$1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-0$-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-A1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-0AT01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-$1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-0$T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01TA1:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T0A:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T$1:01:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T0$:01:01.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T01:A1:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T01:0A:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T01:$1:01.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01T01:0$:01.123"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01 01:01:A1.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01 01:01:0A.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01 01:01:$1.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-01 01:01:0$.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000/01/01 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000/01/01T01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("01/01/2000 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("01/01/2000T01:01:0$.123"));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-0A 01:01:01+800"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-0A 01:01:01.123Z"));
        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("2000-01-0A 01:01:01.123456789Z"));
    }

    @Test
    public void parseLocalDateTime9() {
        char[] chars = "1900-01-1".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                for (int d = 1; d <= 9; d++) {
                    IOUtils.getChars(d, 9, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str, 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            DateUtils.parseLocalDateTime(str, 0, 9),
                            str
                    );
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime9_1() {
        char[] chars = "1900-1-01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtils.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[7] = '0';
                    IOUtils.getChars(d, 9, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str, 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            DateUtils.parseLocalDateTime(str, 0, 9),
                            str
                    );
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime9_2() {
        char[] chars = "1900年1月1日".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtils.getChars(month, 6, chars);

                for (int d = 1; d <= 9; d++) {
                    IOUtils.getChars(d, 8, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str, 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            DateUtils.parseLocalDateTime(str, 0, 9),
                            str
                    );
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime10() {
        char[] chars = "1900-01-01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate10(str, 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            DateUtils.parseLocalDateTime(str, 0, 10),
                            str
                    );
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime11() {
        char[] chars = "1900年01月01日".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate11(str, 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            DateUtils.parseLocalDateTime(str, 0, 11),
                            str
                    );
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime16() {
        char[] chars = "19000101T000000Z".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[4] = '0';
                IOUtils.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[6] = '0';
                    IOUtils.getChars(d, 8, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[9] = '0';
                        IOUtils.getChars(h, 11, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 16),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime16_1() {
        char[] chars = "1900-01-01 00:00".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtils.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 16),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime16_2() {
        assertEquals(
                LocalDateTime.of(2021, 7, 8, 4, 5, 6),
                DateUtils.parseLocalDateTime("2021-07-08T4:5:6", 0, 16)
        );
        assertEquals(
                LocalDateTime.of(2021, 7, 8, 4, 5, 6),
                DateUtils.parseLocalDateTime("2021-07-08 4:5:6", 0, 16)
        );
    }

    @Test
    public void parseLocalDateTime18() {
        char[] chars = "1900-01-01 00:00:1".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtils.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 18),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime18_1() {
        char[] chars = "1900-01-01 00:1:01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtils.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 18),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime18_2() {
        char[] chars = "1900-01-01 0:01:01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    for (int h = 0; h <= 9; h++) {
                        IOUtils.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 18),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime18_3() {
        char[] chars = "1900-01-1 00:01:01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= 9; d++) {
                    IOUtils.getChars(d, 9, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[10] = '0';
                        IOUtils.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 18),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime18_4() {
        char[] chars = "1900-1-01 00:01:01".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtils.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[7] = '0';
                    IOUtils.getChars(d, 9, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[10] = '0';
                        IOUtils.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 18),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseLocalDateTime19() {
        char[] chars = "1900-01-01 00:00:00".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtils.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[8] = '0';
                    IOUtils.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtils.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime19(str, 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime(str, 0, 19),
                                str
                        );
                    }
                }
            }
        }
    }

    @Test
    public void parseZonedDateTime_0() {
        String str = "2000-01-02 03:04:05.1Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 100_000_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_1() {
        String str = "2000-01-02 03:04:05.12Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 120_000_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_2() {
        String str = "2000-01-02 03:04:05.123Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_000_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_3() {
        String str = "2000-01-02 03:04:05.1234Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_400_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_4() {
        String str = "2000-01-02 03:04:05.12345Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_450_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_5() {
        String str = "2000-01-02 03:04:05.123456Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_6() {
        String str = "2000-01-02 03:04:05.1234567Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);
        LocalDateTime actual = zdt.toLocalDateTime();

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_700);

        assertEquals(ldt, actual);
    }

    @Test
    public void parseZonedDateTime_7() {
        String str = "2000-01-02 03:04:05.12345678Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_780);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_8() {
        String str = "2000-01-02 03:04:05.123456789Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_789);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_9() {
        String str = "2000-01-02 03:04Z[UTC]";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 0, 0);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void parseZonedDateTime_10() {
        String str = "2000-01-02 03:04:05.123";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(DEFAULT_ZONE_ID.getId(), zdt.getZone().getId());
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_000_000);
        assertEquals(ldt, zdt.toLocalDateTime());
    }

    @Test
    public void toMillis19() {
        String str = "2022/04/29 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(str, fmt);
        assertEquals(
                ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.toMillis19(str, 0, null)
        );

        String str1 = "1970-01-01 00:00:00";
        DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt1 = LocalDateTime.parse(str1, fmt1);
        assertEquals(
                ldt1.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.toMillis19("0000-00-00 00:00:00", 0, null)
        );

        assertThrows(NullPointerException.class, () -> DateUtils.toMillis19(null, 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("xxx", 0, null));

        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("A000-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2A00-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("20A0-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("200A-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-A1-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-0A-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-A2 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-0A 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 A3:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 0A:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:A4:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:0A:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:04:A5", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:04:0A", 0, null));

        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("$000-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2$00-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("20$0-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("200$-01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-$1-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-0$-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-$2 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-0$ 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 $3:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 0$:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:$4:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:0$:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:04:$5", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:04:0$", 0, null));

        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000X01-02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01X02 03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02X03:04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03X04:05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02 03:04X05", 0, null));
        assertThrows(DateTimeParseException.class, () -> DateUtils.toMillis19("2000-01-02T03:04:0A", 0, null));
    }

    @Test
    public void date2String() {
        Date date = new Date(1664010843321L);

        assertEquals("2022-09-24 17:14:03.321", DateUtils.toString(date));
        assertEquals("2022-09-24 17:14:03.321", DateUtils.toString(date.getTime(), false, SHANGHAI_ZONE_ID));
        assertEquals("2022-09-24 17:14:03.321+08:00", DateUtils.toString(date.getTime(), true, SHANGHAI_ZONE_ID));
        assertEquals("2022-09-24 09:14:03.321", DateUtils.toString(date.getTime(), false, UTC));
        assertEquals("2022-09-24 09:14:03.321Z", DateUtils.toString(date.getTime(), true, UTC));

        assertEquals("2022-09-24 16:14:03.321+07:00", DateUtils.toString(date.getTime(), true, ZoneId.of("GMT+7")));
        assertEquals("2022-09-24 02:14:03.321-07:00", DateUtils.toString(date.getTime(), true, ZoneId.of("GMT-7")));
        assertEquals("2022-09-24 17:29:03.321+08:15", DateUtils.toString(date.getTime(), true, ZoneId.of("GMT+08:15")));
        assertEquals("2022-09-24 00:59:03.321-08:15", DateUtils.toString(date.getTime(), true, ZoneId.of("GMT-08:15")));
    }

    @Test
    public void date2String1() {
        assertEquals("2022-09-24 17:14:03.001", DateUtils.toString(new Date(1664010843001L)));
    }

    @Test
    public void month() {
        assertEquals(0, DateUtils.month('J', 'u', 'a'));
        assertEquals(0, DateUtils.month('J', 'a', 'a'));
        assertEquals(0, DateUtils.month('F', 'a', 'a'));
        assertEquals(0, DateUtils.month('F', 'e', 'a'));
        assertEquals(0, DateUtils.month('A', 'e', 'a'));
        assertEquals(0, DateUtils.month('A', 'p', 'a'));
        assertEquals(0, DateUtils.month('M', 'p', 'a'));
        assertEquals(0, DateUtils.month('M', 'a', 'a'));
        assertEquals(0, DateUtils.month('J', 'a', 'a'));
        assertEquals(0, DateUtils.month('J', 'u', 'a'));
        assertEquals(0, DateUtils.month('A', 'a', 'a'));
        assertEquals(0, DateUtils.month('A', 'u', 'a'));
        assertEquals(0, DateUtils.month('S', 'u', 'a'));
        assertEquals(0, DateUtils.month('S', 'e', 'a'));
        assertEquals(0, DateUtils.month('O', 'e', 'a'));
        assertEquals(0, DateUtils.month('O', 'c', 'a'));
        assertEquals(0, DateUtils.month('N', 'c', 'a'));
        assertEquals(0, DateUtils.month('N', 'o', 'a'));
        assertEquals(0, DateUtils.month('D', 'o', 'a'));
        assertEquals(0, DateUtils.month('D', 'e', 'a'));
        assertEquals(0, DateUtils.month('K', 'e', 'a'));

        String[] strings = new String[]{
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
        };

        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            char c0 = str.charAt(0);
            char c1 = str.charAt(1);
            char c2 = str.charAt(2);
            assertEquals(i + 1, DateUtils.month(c0, c1, c2));
        }
    }

    @Test
    public void test() {
        Date date = DateUtils.parseDate("Dec 7, 2022 10:55:19 AM");
        ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
        assertEquals(2022, zdt.getYear());
        assertEquals(12, zdt.getMonthValue());
        assertEquals(7, zdt.getDayOfMonth());
        assertEquals(10, zdt.getHour());
        assertEquals(55, zdt.getMinute());
        assertEquals(19, zdt.getSecond());
    }

    @Test
    public void testMonth() {
        String[] months = {
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
        };

        for (int i = 0; i < months.length; i++) {
            String month = months[i];

            for (int day = 1; day < 28; day++) {
                for (int hour = 1; hour <= 9; hour++) {
                    String str = month + " " + day + ", 2022 0" + hour + ":55:19 AM";

                    Date date = DateUtils.parseDate(str);
                    ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.getYear());
                    assertEquals(i + 1, zdt.getMonthValue());
                    assertEquals(day, zdt.getDayOfMonth());
                    assertEquals(hour, zdt.getHour());
                    assertEquals(55, zdt.getMinute());
                    assertEquals(19, zdt.getSecond());
                }

                for (int hour = 1; hour <= 9; hour++) {
                    String str = month + " " + day + ", 2022 " + hour + ":55:19 AM";

                    Date date = DateUtils.parseDate(str);
                    ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.getYear());
                    assertEquals(i + 1, zdt.getMonthValue());
                    assertEquals(day, zdt.getDayOfMonth());
                    assertEquals(hour, zdt.getHour());
                    assertEquals(55, zdt.getMinute());
                    assertEquals(19, zdt.getSecond());
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = month + " " + day + ", 2022 1" + hour + ":55:19 AM";

                    Date date = DateUtils.parseDate(str);
                    ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.getYear());
                    assertEquals(i + 1, zdt.getMonthValue());
                    assertEquals(day, zdt.getDayOfMonth());
                    assertEquals(hour + 10, zdt.getHour());
                    assertEquals(55, zdt.getMinute());
                    assertEquals(19, zdt.getSecond());
                }

                for (int hour = 0; hour <= 9; hour++) {
                    String str = month + " " + day + ", 2022 0" + hour + ":55:19 PM";

                    Date date = DateUtils.parseDate(str);
                    ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.getYear());
                    assertEquals(i + 1, zdt.getMonthValue());
                    assertEquals(day, zdt.getDayOfMonth());
                    assertEquals(hour + 12, zdt.getHour());
                    assertEquals(55, zdt.getMinute());
                    assertEquals(19, zdt.getSecond());
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = month + " " + day + ", 2022 1" + hour + ":55:19 PM";

                    Date date = DateUtils.parseDate(str);
                    ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.getYear());
                    assertEquals(i + 1, zdt.getMonthValue());
                    assertEquals(day, zdt.getDayOfMonth());
                    assertEquals(hour + 22, zdt.getHour());
                    assertEquals(55, zdt.getMinute());
                    assertEquals(19, zdt.getSecond());
                }
            }
        }
    }
}
