package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.time.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;

import static com.alibaba.fastjson2.time.ZoneId.*;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {
    Locale locale;
    @BeforeEach
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void parseDateNullOrEmpty() {
        assertNull(DateUtils.parseDate("", DEFAULT_ZONE_ID));
        assertNull(DateUtils.parseDate("", "yyyy-MM-dd HH:mm:ss", DEFAULT_ZONE_ID));
        assertNull(DateUtils.parseDate("null", DEFAULT_ZONE_ID));
        assertNull(DateUtils.parseDate("null", "yyyy-MM-dd HH:mm:ss", DEFAULT_ZONE_ID));
        assertEquals(0, DateUtils.parseMillis("null", DEFAULT_ZONE_ID));
        assertEquals(0, DateUtils.parseMillis("", DEFAULT_ZONE_ID));
        assertEquals(0, DateUtils.parseMillis(null, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseDate0() {
        String str = "2022-04-29 12:13:14";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, null, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, "", DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, null).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseMillis19(str, DEFAULT_ZONE_ID, DateUtils.DateTimeFormatPattern.DATE_TIME_FORMAT_19_DASH)
        );
        assertEquals(
                millis,
                DateUtils.parseMillis(str, DEFAULT_ZONE_ID)
        );
    }

    @Test
    public void parseDate0_11() {
        String str = "2022-11-29 12:13:14";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, null, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, "", DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, null).getTime()
        );
    }

    @Test
    public void parseDate0_t() {
        String str = "2022-04-29T12:13:14";
        String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate0_slash() {
        String str = "2022/02/28 12:13:14";
        String pattern = "yyyy/MM/dd HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, null).getTime()
        );
    }

    @Test
    public void parseDate0_dot() {
        String str = "29.11.2022 12:13:14";
        String pattern = "dd.MM.yyyy HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate0_x() {
        String str = "4 Dec 2022 12:13:14";
        String pattern = "d MMM yyyy HH:mm:ss";
        LocalDateTime ldt = LocalDateTime.of(2022, 12, 4, 12, 13, 14);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, pattern, null).getTime()
        );
    }

    @Test
    public void parseDate1() {
        String str = "2022-04-29";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate3() {
        String str = "2022-4-29";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-dd");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate4() {
        long millis = System.currentTimeMillis();
        String str = Long.toString(millis);
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate5() {
        String str = "2022-04-29 12:13:14Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str.substring(0, 19));
        assertEquals(
                ZonedDateTime.of(ldt, UTC).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate6() {
        String str = "2022-04-29 12:13Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime ldt = fmt.parseLocalDateTime(str.substring(0, 16));
        assertEquals(
                ZonedDateTime.of(ldt, UTC).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate7() {
        String str = "2022-11-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDate_L7_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("202212X", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("202212X".getBytes(), 0, 7));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("202212X".toCharArray(), 0, 7));
    }

    @Test
    public void parseDate8() {
        String str = "2022-1-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime());

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate8_1() {
        String str = "20221112";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime());

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }
    @Test
    public void parseDate8_2() {
        String str = "20221012";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime());

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate9() {
        String str = "2022/04/29 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime());

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L8() {
        String str = "20220203";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 2, 3), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID)
                .toInstant()
                .toEpochMilli();
        assertEquals(millis, DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime());
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9() {
        String str = "2022년1월2일";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 1, 2), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID)
                .toInstant()
                .toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID)
                        .getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
    }

    @Test
    public void parseDate_L9_1() {
        String str = "2022-11-2";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 11, 2), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID)
                .toInstant()
                .toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID)
                        .getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_2() {
        String str = "2022-1-12";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 1, 12), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID)
                .toInstant()
                .toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID)
                        .getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_3() {
        String str = "2022/12/1";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/M/d");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_4() {
        String str = "2022/1/21";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/M/d");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_5() {
        String str = "1.12.2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_6() {
        String str = "12.1.2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_7() {
        String str = "1-12-2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d-M-yyyy");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L9_8() {
        String str = "12-1-2022";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d-M-yyyy");
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L10() {
        String str = "2022년11월12일";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2022, 11, 12), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
    }

    @Test
    public void parseDate_L10_1() {
        String str = "2021-02-02";
        LocalDateTime ldt = LocalDateTime.of(LocalDate.of(2021, 2, 2), LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L10_2() {
        String str = "2022-11-29";
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = fmt.parseLocalDate(str);
        LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L10_3() {
        String str = "2022/11/29";
        String pattern = "yyyy/MM/dd";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = fmt.parseLocalDate(str);
        LocalDateTime ldt = LocalDateTime.of(localDate, LocalTime.MIN);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        assertEquals(
                millis,
                DateUtils.parseDate(str, pattern, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L10_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("6 XXX 2021"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("6 XXX 2021".toCharArray(), 0, 15));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("6 XXX 2021".getBytes(), 0, 15));
        assertNull(DateUtils.parseLocalDate10("6 XXX 2021".toCharArray(), 0));
        assertNull(DateUtils.parseLocalDate10("6 XXX 2021".getBytes(), 0));
    }

    @Test
    public void parseDate_L11_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021", DEFAULT_ZONE_ID));
        assertNull(DateUtils.parseLocalDate("16 XXX 2021".getBytes(), 0, 11));
        assertNull(DateUtils.parseLocalDate("16 XXX 2021".toCharArray(), 0, 11));
        assertNull(DateUtils.parseLocalDate11("16 XXX 2021".getBytes(), 0));
        assertNull(DateUtils.parseLocalDate11("16 XXX 2021".toCharArray(), 0));
    }

    @Test
    public void parseDate_L16_0() {
        String str = "6 Dec 2021 1:2:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 1, 2, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L16_1() {
        String str = "2011-12-03+01:00";
        ZoneId zoneId = ZoneId.of("+01:00");
        LocalDateTime ldt = LocalDateTime.of(2011, 12, 3, 0, 0, 0);
        long millis = ZonedDateTime.of(ldt, zoneId).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L16_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021 1:2:3", DEFAULT_ZONE_ID));

        String[] strings = new String[] {
                "2011X12",
                "2011X12-03+01:00",
                "2011-12X03+01:00",
                "2011-12-03X01:00",

                "2011-12-03X01:00",
                "2011-12-03+01X00",
                "201X-12-03+01:00",
                "20X1-12-03+01:00",
                "2X11-12-03+01:00",

                "X011-12-03+01:00",
                "2011-X2-03+01:00",
                "2011-1X-03+01:00",
                "2011-12-X3+01:00",
                "2011-12-0X+01:00"
        };

        for (String string : strings) {
            assertThrows(Exception.class, () -> DateUtils.parseZonedDateTime16(string.toCharArray(), 0, DEFAULT_ZONE_ID));
            assertThrows(Exception.class, () -> DateUtils.parseZonedDateTime16(string.getBytes(), 0, DEFAULT_ZONE_ID));
        }
    }

    @Test
    public void parseDate_L17_0() {
        String str = "6 Dec 2021 1:2:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 1, 2, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L17_1() {
        String str = "6 Dec 2021 1:12:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 1, 12, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L17_2() {
        String str = "6 Dec 2021 11:2:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 11, 2, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L17_3() {
        String str = "16 Dec 2021 1:2:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 1, 2, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L17_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 1:2:3", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("1 XXX 2021 11:2:3", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("1 XXX 2021 1:12:3", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("1 XXX 2021 1:2:13", DEFAULT_ZONE_ID));
    }

    @Test
    public void parseDate_L18_0() {
        String str = "2021-02-02 10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 2, 2, 10, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_1() {
        String str = "6 Dec 2021 1:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 1, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_2() {
        String str = "6 Dec 2021 11:2:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 11, 2, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_3() {
        String str = "6 Dec 2021 11:12:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 11, 12, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_4() {
        String str = "16 Dec 2021 11:2:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 11, 2, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_5() {
        String str = "16 Dec 2021 1:12:3";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 1, 12, 3);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_6() {
        String str = "16 Dec 2021 1:2:13";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 1, 2, 13);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_7() {
        String str = "16 Dec 1960 1:2:13";
        LocalDateTime ldt = LocalDateTime.of(1960, 12, 16, 1, 2, 13);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L18_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 11:2:3", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 1:12:3", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 1:2:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021 1:12:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021 11:2:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021 11:12:3", DEFAULT_ZONE_ID));
    }

    @Test
    public void parseDate_L19_0() {
        String str = "2021-02-02  10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 2, 2, 10, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_1() {
        String str = "23/06/2021 10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 6, 23, 10, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_2() {
        String str = "6 Dec 2021 10:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 6, 10, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_3() {
        String str = "16 Dec 2021 1:12:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 1, 12, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_4() {
        String str = "16 Dec 2021 12:3:34";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 12, 3, 34);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_5() {
        String str = "16 Dec 2021 12:13:4";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 12, 13, 4);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L19_error() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("6 XXX 2021 11:12:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 1:12:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 11:2:13", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseDate("16 XXX 2021 11:12:3", DEFAULT_ZONE_ID));
    }

    @Test
    public void parseDate_L20_0() {
        String str = "16 Dec 2021 12:13:14";
        LocalDateTime ldt = LocalDateTime.of(2021, 12, 16, 12, 13, 14);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L20_1() {
        String str = "16 Dec 1960 12:13:14";
        LocalDateTime ldt = LocalDateTime.of(1960, 12, 16, 12, 13, 14);
        long millis = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDate_L20_2() {
        String str = "16 Dec 1960 12:13:14";
        LocalDateTime ldt = LocalDateTime.of(1960, 12, 16, 12, 13, 14);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime20(str.toCharArray(), 0)
        );
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime20(str.getBytes(), 0)
        );
    }

    @Test
    public void parseDate_L20_error() {
        String[] strings = new String[] {
                "16_Dec 1960 12:13:14",
                "16 Dec_1960 12:13:14",
                "16 Dec 1960_12:13:14",
                "16 Dec 1960 12_13:14",
                "16 Dec 1960 12:13_14",
                "16 Dec 1960 32:13:14",
                "6 XXX 2021"
        };
        for (String string : strings) {
            assertNull(DateUtils.parseLocalDateTime20(string.getBytes(), 0), string);
            assertNull(DateUtils.parseLocalDateTime20(string.toCharArray(), 0), string);
        }
    }

    @Test
    public void parseDateZ() {
        String str = "2022-04-29T00:00:00Z";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str.substring(0, 19));
        ZoneId zoneId = UTC;
        assertEquals(
                ZonedDateTime.of(ldt, zoneId).toInstant().toEpochMilli(),
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
    }

    @Test
    public void parseDateZ1() {
        String str = "2022-04-29T00:00:00+08:00";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str.substring(0, 19));
        ZoneId zoneId = ZoneId.of("GMT+8");
        long millis = ZonedDateTime.of(ldt, zoneId).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseDateZ2() {
        String str = "2022-04-29T00:00:00[Asia/Shanghai]";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str.substring(0, 19));
        long millis = ZonedDateTime.of(ldt, SHANGHAI_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(
                millis,
                DateUtils.parseDate(str, DEFAULT_ZONE_ID).getTime()
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(millis, DateUtils.parseMillis(chars, 0, chars.length));
        assertEquals(millis, DateUtils.parseMillis(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDate_L8() {
        String str = "20220112";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = fmt.parseLocalDate(str);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(localDate, DateUtils.parseLocalDate(chars, 0, chars.length));
        assertEquals(localDate, DateUtils.parseLocalDate(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDate_L8_1() {
        String str = "2022-1-2";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate = fmt.parseLocalDate(str);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(localDate, DateUtils.parseLocalDate(chars, 0, chars.length));
        assertEquals(localDate, DateUtils.parseLocalDate(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDate_NullOrError() {
        assertNull(DateUtils.parseLocalDate(null));
        assertNull(DateUtils.parseLocalDate((char[]) null, 0, 0));
        assertNull(DateUtils.parseLocalDate((byte[]) null, 0, 0));
        assertNull(DateUtils.parseLocalDate(""));
        assertNull(DateUtils.parseLocalDate("null"));
        assertNull(DateUtils.parseLocalDate("00000000"));
    }

    @Test
    public void parseLocalDate_L8_NullOrError() {
        assertNull(DateUtils.parseLocalDate("00000000"));

        assertNull(DateUtils.parseLocalDate8("00".getBytes(), 0));
        assertNull(DateUtils.parseLocalDate8("00".toCharArray(), 0));

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

        assertNull(DateUtils.parseLocalDate9("00".getBytes(), 0));
        assertNull(DateUtils.parseLocalDate9("00".toCharArray(), 0));

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
        LocalDate localDate = fmt.parseLocalDate(str);
        assertEquals(
                localDate,
                DateUtils.parseLocalDate(str)
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(localDate, DateUtils.parseLocalDate(chars, 0, chars.length));
        assertEquals(localDate, DateUtils.parseLocalDate(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDate_L10_NullOrError() {
        assertNull(DateUtils.parseLocalDate("0000-00-00"));

        assertNull(DateUtils.parseLocalDate10("00".toCharArray(), 0));
        assertNull(DateUtils.parseLocalDate10("00".getBytes(), 0));

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

        assertNull(DateUtils.parseLocalDate11("00".getBytes(), 0));
        assertNull(DateUtils.parseLocalDate11("00".toCharArray(), 0));

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
        LocalDateTime ldt = LocalDateTime.of(fmt.parseLocalDate(str), LocalTime.MIN);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str, 0, str.length())
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, chars.length));
        assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDateTime_1() {
        String str = "2022-01-02 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str, 0, str.length())
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, chars.length));
        assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDateTime_L14() {
        String str = "20220102121314";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str, 0, str.length())
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, chars.length));
        assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDateTime_L12() {
        String str = "202201021213";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ldt,
                DateUtils.parseLocalDateTime(str, 0, str.length())
        );
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, chars.length));
        assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));
    }

    @Test
    public void parseLocalDateTime_NullOrError() {
        assertNull(parseLocalDateTime(null));
        assertNull(DateUtils.parseLocalDateTime((String) null, 0, 0));
        assertNull(DateUtils.parseLocalDateTime((char[]) null, 0, 0));
        assertNull(DateUtils.parseLocalDateTime((byte[]) null, 0, 0));
        assertNull(parseLocalDateTime(""));
        assertNull(parseLocalDateTime("null"));
        assertNull(parseLocalDateTime("000000000000"));
        assertNull(parseLocalDateTime("00000000"));
        assertNull(parseLocalDateTime("0000-00-0"));
        assertNull(parseLocalDateTime("0000-00-00"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("xxxx"));
    }

    public static LocalDateTime parseLocalDateTime(String str) {
        if (str == null) {
            return null;
        }
        return DateUtils.parseLocalDateTime(str, 0, str.length());
    }

    @Test
    public void parseLocalDateTime_L12_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime12("00".getBytes(), 0));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime12("00".toCharArray(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("202A01021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20A001021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2A0001021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A00001021213"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$00001021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2$0001021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20$001021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200$01021213"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000A0021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000A021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000$0021213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000$021213"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010A1213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001A01213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010$1213"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001$01213"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101A013"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010A13"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101$013"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010$13"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101010A"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101A0"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101010$"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101$0"));
    }

    @Test
    public void parseLocalDateTime_L14_NullOrError() {
        assertNull(DateUtils.parseLocalDateTime14("00".toCharArray(), 0));
        assertNull(DateUtils.parseLocalDateTime14("00".getBytes(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A0000101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("0A000101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("00A00101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("000A0101010101"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$0000101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("0$000101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("00$00101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("000$0101010101"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000A101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000A01010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000$101010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000$01010101"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001A1010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010A010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001$1010101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010$010101"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101A00101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010A0101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101$00101"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010$0101"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101A001"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101010A01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101$001"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101010$01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010101A0"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101010A"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001010101$0"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010101010$"));
    }

    @Test
    public void parseLocalDateTime_L16_NullOrError() {
        assertNull(DateUtils.parseLocalDateTime16("00".toCharArray(), 0));
        assertNull(DateUtils.parseLocalDateTime16("00".getBytes(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101X010101Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A0000101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("0A000101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("00A00101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("000A0101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$0000101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("0$000101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("00$00101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("000$0101T010101Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000A101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000A01T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000$101T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000$01T010101Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001A1T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010AT010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200001$1T010101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000010$T010101Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101TA00101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T0A0101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T$00101Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T0$0101Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T01A001Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T010A01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T01$001Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T010$01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T0101A0Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T01010AZ"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T0101$0Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20000101T01010$Z"));
    }

    @Test
    public void parseLocalDateTime_L17_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime17("00".toCharArray(), 0));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime17("00".getBytes(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:01X"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A000-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2A00-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20A0-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200A-01-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$000-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2$00-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20$0-01-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200$-01-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-A1-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0A-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-$1-01T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0$-01T01:01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-A1T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0AT01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-$1T01:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0$T01:01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01TA1:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0A:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T$1:01Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0$:01Z"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:A1Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0AZ"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:$1Z"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0$Z"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-1 01:01:A1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-1 01:01:0A"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-1 01:01:$1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-1 01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_L18_NullOrError() {
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime18("00".toCharArray(), 0));
        assertThrows(DateTimeException.class, () -> DateUtils.parseLocalDateTime18("00".getBytes(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01X01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01A01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A000-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2A00-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20A0-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200A-1-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$000-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2$00-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20$0-1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200$-1-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-A1-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0A-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-$1-1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0$-1T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-A1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-0AT01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-$1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-0$T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01TA1:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T0A:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T$1:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T0$:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T01:A1:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T01:0A:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T01:$1:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01T01:0$:01"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01 01:01:A1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01 01:01:0A"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01 01:01:$1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-1-01 01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_L19_NullOrError() {
        assertNull(DateUtils.parseLocalDateTime19("00".toCharArray(), 0));
        assertNull(DateUtils.parseLocalDateTime19("00".getBytes(), 0));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01A01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A000-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2A00-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20A0-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200A-01-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$000-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2$00-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20$0-01-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200$-01-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-A1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0A-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-$1-01T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0$-01T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-A1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0AT01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-$1T01:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0$T01:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01TA1:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0A:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T$1:01:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0$:01:01"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:A1:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0A:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:$1:01"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0$:01"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:A1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:0A"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:$1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:0$"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000/01/01 01:01:0$"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000/01/01T01:01:0$"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("01/01/2000 01:01:0$"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("01/01/2000T01:01:0$"));
    }

    @Test
    public void parseLocalDateTime_X_NullOrError() {
        assertNull(DateUtils.parseLocalDateTimeX((char[]) null, 0, 0));
        assertNull(DateUtils.parseLocalDateTimeX((byte[]) null, 0, 0));
        assertNull(DateUtils.parseLocalDateTimeX("".getBytes(), 0, 0));
        assertNull(DateUtils.parseLocalDateTimeX("".toCharArray(), 0, 0));

        assertNull(DateUtils.parseLocalDateTimeX("00".toCharArray(), 0, 1));
        assertNull(DateUtils.parseLocalDateTimeX("00".getBytes(), 0, 1));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.1"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.12"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.1234"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.12345"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.123456"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.1234567"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.12345678"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01X01:01:01.123456789"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01A01:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("A000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2A00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20A0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200A-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("$000-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2$00-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("20$0-01-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("200$-01-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-A1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0A-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-$1-01T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-0$-01T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-A1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0AT01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-$1T01:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-0$T01:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01TA1:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0A:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T$1:01:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T0$:01:01.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:A1:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0A:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:$1:01.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01T01:0$:01.123"));

        // yyyy-M-d HH:mm:ss
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:A1.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:0A.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:$1.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000-01-01 01:01:0$.123"));

        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000/01/01 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("2000/01/01T01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("01/01/2000 01:01:0$.123"));
        assertThrows(DateTimeException.class, () -> parseLocalDateTime("01/01/2000T01:01:0$.123"));
    }

    @Test
    public void parseZonedDateTime_X_NullOrError() {
        assertNull(DateUtils.parseZonedDateTime(null));
        assertNull(DateUtils.parseZonedDateTime(""));

        assertThrows(DateTimeException.class, () -> DateUtils.parseZonedDateTime("00"));

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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                for (int d = 1; d <= 9; d++) {
                    IOUtilsTest.getChars(d, 9, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate(str.getBytes(), 0, 9), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str.getBytes(), 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str.toCharArray(), 0), LocalTime.MIN),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtilsTest.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 9, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str.toCharArray(), 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str.getBytes(), 0), LocalTime.MIN),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtilsTest.getChars(month, 6, chars);

                for (int d = 1; d <= 9; d++) {
                    IOUtilsTest.getChars(d, 8, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate9(str.toCharArray(), 0), LocalTime.MIN),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate10(str.getBytes(), 0), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate10(str.toCharArray(), 0), LocalTime.MIN),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    String str = new String(chars);

                    LocalDateTime of = LocalDateTime.of(year, month, d, 0, 0, 0);
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate(str.toCharArray(), 0, 11), LocalTime.MIN),
                            str
                    );
                    assertEquals(
                            of,
                            LocalDateTime.of(DateUtils.parseLocalDate11(str.toCharArray(), 0), LocalTime.MIN),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[4] = '0';
                IOUtilsTest.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 8, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[9] = '0';
                        IOUtilsTest.getChars(h, 11, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtilsTest.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime16(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtilsTest.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtilsTest.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    for (int h = 0; h <= 9; h++) {
                        IOUtilsTest.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= 9; d++) {
                    IOUtilsTest.getChars(d, 9, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[10] = '0';
                        IOUtilsTest.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 9; month++) {
                IOUtilsTest.getChars(month, 6, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 9, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[10] = '0';
                        IOUtilsTest.getChars(h, 12, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 1, 1);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.toCharArray(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime18(str.getBytes(), 0),
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
            IOUtilsTest.getChars(year, 4, chars);

            for (int month = 1; month <= 12; month++) {
                chars[5] = '0';
                IOUtilsTest.getChars(month, 7, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (LocalDate.isLeapYear(year) ? 29 : 28);
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
                    IOUtilsTest.getChars(d, 10, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[11] = '0';
                        IOUtilsTest.getChars(h, 13, chars);
                        String str = new String(chars);

                        LocalDateTime of = LocalDateTime.of(year, month, d, h, 0, 0);
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime19(str.getBytes(), 0),
                                str
                        );
                        assertEquals(
                                of,
                                DateUtils.parseLocalDateTime19(str.toCharArray(), 0),
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

        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 100_000_000);

        assertEquals(UTC.id, zdt.zone.id);
        assertEquals(ldt, zdt.dateTime);

        byte[] bytes = str.getBytes();
        ZonedDateTime zdt1 = DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID);

        assertEquals(UTC.id, zdt.zone.id);
        assertEquals(ldt, zdt1.dateTime);

        char[] chars = str.toCharArray();
        ZonedDateTime zdt2 = DateUtils.parseZonedDateTime(chars, 0, bytes.length, DEFAULT_ZONE_ID);

        assertEquals(UTC.id, zdt.zone.id);
        assertEquals(ldt, zdt2.dateTime);
    }

    @Test
    public void parseZonedDateTime_1() {
        String str = "2000-01-02 03:04:05.12Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 120_000_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_2() {
        String str = "2000-01-02 03:04:05.123Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_000_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_3() {
        String str = "2000-01-02 03:04:05.1234Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_400_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_4() {
        String str = "2000-01-02 03:04:05.12345Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_450_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_5() {
        String str = "2000-01-02 03:04:05.123456Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_6() {
        String str = "2000-01-02 03:04:05.1234567Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);
        LocalDateTime actual = zdt.dateTime;

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_700);

        assertEquals(ldt, actual);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_7() {
        String str = "2000-01-02 03:04:05.12345678Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_780);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_8() {
        String str = "2000-01-02 03:04:05.123456789Z";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_456_789);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_9() {
        String str = "2000-01-02 03:04Z[UTC]";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(UTC.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 0, 0);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void parseZonedDateTime_10() {
        String str = "2000-01-02 03:04:05.123";
        ZonedDateTime zdt = DateUtils.parseZonedDateTime(str);

        assertEquals(DEFAULT_ZONE_ID.id, zdt.zone.id);
        LocalDateTime ldt = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123_000_000);
        assertEquals(ldt, zdt.dateTime);

        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertEquals(zdt, DateUtils.parseZonedDateTime(chars, 0, chars.length, DEFAULT_ZONE_ID));
        assertEquals(zdt, DateUtils.parseZonedDateTime(bytes, 0, bytes.length, DEFAULT_ZONE_ID));
    }

    @Test
    public void toMillis19() {
        String str = "2022/04/29 12:13:14";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime ldt = fmt.parseLocalDateTime(str);
        assertEquals(
                ZonedDateTime.of(ldt, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseMillis19(str, null)
        );

        String str1 = "1970-01-01 00:00:00";
        DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt1 = fmt.parseLocalDateTime(str1);
        assertEquals(
                ZonedDateTime.of(ldt1, DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                DateUtils.parseMillis19("0000-00-00 00:00:00", null)
        );

        assertThrows(NullPointerException.class, () -> DateUtils.parseMillis19(null, null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("xxx", null));

        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("A000-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2A00-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("20A0-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("200A-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-A1-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-0A-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-A2 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-0A 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 A3:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 0A:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:A4:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:0A:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:04:A5", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:04:0A", null));

        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("$000-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2$00-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("20$0-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("200$-01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-$1-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-0$-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-$2 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-0$ 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 $3:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 0$:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:$4:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:0$:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:04:$5", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:04:0$", null));

        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000X01-02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01X02 03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02X03:04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03X04:05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02 03:04X05", null));
        assertThrows(DateTimeException.class, () -> DateUtils.parseMillis19("2000-01-02T03:04:0A", null));
    }

    @Test
    public void date2String() {
        Date date = new Date(1664010843321L);

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
        assertEquals("2022-09-24 17:14:03.001", DateUtils.toString(1664010843001L, false, DEFAULT_ZONE_ID));
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
        Date date = new Date(DateUtils.parseMillis("Dec 7, 2022 10:55:19 AM", DEFAULT_ZONE_ID));
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
        assertEquals(2022, zdt.dateTime.date.year);
        assertEquals(12, zdt.dateTime.date.monthValue);
        assertEquals(7, zdt.dateTime.date.dayOfMonth);
        assertEquals(10, zdt.dateTime.time.hour);
        assertEquals(55, zdt.dateTime.time.minute);
        assertEquals(19, zdt.dateTime.time.second);
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

                    Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.dateTime.date.year);
                    assertEquals(i + 1, zdt.dateTime.date.monthValue);
                    assertEquals(day, zdt.dateTime.date.dayOfMonth);
                    assertEquals(hour, zdt.dateTime.time.hour);
                    assertEquals(55, zdt.dateTime.time.minute);
                    assertEquals(19, zdt.dateTime.time.second);
                }

                for (int hour = 1; hour <= 9; hour++) {
                    String str = month + " " + day + ", 2022 " + hour + ":55:19 AM";

                    Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.dateTime.date.year);
                    assertEquals(i + 1, zdt.dateTime.date.monthValue);
                    assertEquals(day, zdt.dateTime.date.dayOfMonth);
                    assertEquals(hour, zdt.dateTime.time.hour);
                    assertEquals(55, zdt.dateTime.time.minute);
                    assertEquals(19, zdt.dateTime.time.second);
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = month + " " + day + ", 2022 1" + hour + ":55:19 AM";

                    Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.dateTime.date.year);
                    assertEquals(i + 1, zdt.dateTime.date.monthValue);
                    assertEquals(day, zdt.dateTime.date.dayOfMonth);
                    assertEquals(hour + 10, zdt.dateTime.time.hour);
                    assertEquals(55, zdt.dateTime.time.minute);
                    assertEquals(19, zdt.dateTime.time.second);
                }

                for (int hour = 0; hour <= 9; hour++) {
                    String str = month + " " + day + ", 2022 0" + hour + ":55:19 PM";

                    Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.dateTime.date.year);
                    assertEquals(i + 1, zdt.dateTime.date.monthValue);
                    assertEquals(day, zdt.dateTime.date.dayOfMonth);
                    assertEquals(hour + 12, zdt.dateTime.time.hour);
                    assertEquals(55, zdt.dateTime.time.minute);
                    assertEquals(19, zdt.dateTime.time.second);
                }

                for (int hour = 0; hour <= 1; hour++) {
                    String str = month + " " + day + ", 2022 1" + hour + ":55:19 PM";

                    Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
                    assertEquals(2022, zdt.dateTime.date.year);
                    assertEquals(i + 1, zdt.dateTime.date.monthValue);
                    assertEquals(day, zdt.dateTime.date.dayOfMonth);
                    assertEquals(hour + 22, zdt.dateTime.time.hour);
                    assertEquals(55, zdt.dateTime.time.minute);
                    assertEquals(19, zdt.dateTime.time.second);
                }
            }
        }
    }

    @Test
    public void parseLocalDate8() {
        String str = "1-Nov-19";
        LocalDate expect = LocalDate.of(2019, 11, 1);
        assertEquals(expect, DateUtils.parseLocalDate8(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate8(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate8_1() {
        String str = "2/1/2019";
        LocalDate expect = LocalDate.of(2019, 2, 1);
        assertEquals(expect, DateUtils.parseLocalDate8(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate8(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate9() {
        String str = "31-May-19";
        LocalDate expect = LocalDate.of(2019, 5, 31);
        assertEquals(expect, DateUtils.parseLocalDate9(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate9(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate9_1() {
        String str = "10/1/2019";
        LocalDate expect = LocalDate.of(2019, 10, 1);
        assertEquals(expect, DateUtils.parseLocalDate9(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate9(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate9_2() {
        String str = "1/10/2019";
        LocalDate expect = LocalDate.of(2019, 1, 10);
        assertEquals(expect, DateUtils.parseLocalDate9(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate9(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate10() {
        String str = "10/11/2019";
        LocalDate expect = LocalDate.of(2019, 10, 11);
        assertEquals(expect, DateUtils.parseLocalDate(str.toCharArray(), 0, 10));
        assertEquals(expect, DateUtils.parseLocalDate10(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate(str.getBytes(), 0, 10));
        assertEquals(expect, DateUtils.parseLocalDate10(str.getBytes(), 0));
    }

    @Test
    public void parseLocalDate10_1() {
        String str = "2019-10-11";
        LocalDate expect = LocalDate.of(2019, 10, 11);
        assertEquals(expect, DateUtils.parseLocalDate10(str.toCharArray(), 0));
        assertEquals(expect, DateUtils.parseLocalDate10(str.getBytes(), 0));
    }

    @Test
    public void parseMillis19() {
        String str = "2019-10-11 12:13:14";
        LocalDateTime ldt = LocalDateTime.of(2019, 10, 11, 12, 13, 14);
        long millis = ZonedDateTime.of(ldt, ZoneId.DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseMillis19(str, ZoneId.DEFAULT_ZONE_ID));
        assertEquals(millis, DateUtils.parseMillis19(str.getBytes(), 0, ZoneId.DEFAULT_ZONE_ID));
    }

    @Test
    public void parseDate22() {
        String str = "04/03/2023 12:13:14 AM";
        LocalDateTime ldt = LocalDateTime.of(2023, 4, 3, 12, 13, 14);
        long millis = ZonedDateTime.of(ldt, ZoneId.DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        assertEquals(millis, DateUtils.parseMillis(str, ZoneId.DEFAULT_ZONE_ID));
    }

    @Test
    public void testNull() {
        String str = "null";
        byte[] bytes = str.getBytes();
        char[] chars = str.toCharArray();

        assertNull(DateUtils.parseLocalDateTime(bytes, 0, bytes.length));
        assertNull(DateUtils.parseLocalDateTime(chars, 0, bytes.length));
    }
}
