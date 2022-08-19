package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.LocalDateTime1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeTest {
    static LocalDateTime[] dateTimes = new LocalDateTime[]{
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 10),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 100),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000_0),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000_00),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_0),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 1000_000_00),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 9),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 99),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_9),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_99),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_999),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_9),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_99),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 999_999_999),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 121_000_000),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 140__000_000),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 200__000_000),
            LocalDateTime.of(2021, 10, 20, 16, 22, 15, 0)
    };

    @Test
    public void test_jsonb() {
        for (int i = 0; i < dateTimes.length; i++) {
            LocalDateTime dateTime = dateTimes[i];
            LocalDateTime1 vo = new LocalDateTime1();
            vo.setDate(dateTime);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            LocalDateTime1 v1 = JSONB.parseObject(jsonbBytes, LocalDateTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_jsonb_str() {
        for (int i = 0; i < dateTimes.length; i++) {
            LocalDateTime dateTime = dateTimes[i];
            String str = dateTime.toString();
            byte[] jsonbBytes = JSONB.toBytes(str);

            LocalDateTime ldt = JSONB.parseObject(jsonbBytes, LocalDateTime.class);
            assertEquals(dateTime, ldt);
        }
    }

    // [102, 50, 48, 50, 49, 45, 49, 48, 45, 50, 48, 84, 49, 54, 58, 50, 50, 58, 49, 53, 46, 48, 48, 48, 48, 48, 48, 48, 48, 49]
    // [102, 50, 0, 48, 0, 50, 0, 49, 0, 45, 0, 49, 0, 48, 0, 45, 0, 50, 0, 48, 0, 84, 0, 49, 0, 54, 0, 58, 0, 50]

    @Test
    public void test_jsonb_str_1() {
        for (LocalDateTime dateTime : dateTimes) {
            String str = (String) JSON.parse(JSON.toJSONString(dateTime));
            byte[] jsonbBytes = JSONB.toBytes(str);

            LocalDateTime ldt = JSONB.parseObject(jsonbBytes, LocalDateTime.class);
            assertEquals(dateTime, ldt);
        }
    }

    @Test
    public void test_utf8() {
        for (LocalDateTime dateTime : dateTimes) {
            LocalDateTime1 vo = new LocalDateTime1();
            vo.setDate(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            LocalDateTime1 v1 = JSON.parseObject(utf8Bytes, LocalDateTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_str() {
        for (LocalDateTime dateTime : dateTimes) {
            LocalDateTime1 vo = new LocalDateTime1();
            vo.setDate(dateTime);
            String str = JSON.toJSONString(vo);

            LocalDateTime1 v1 = JSON.parseObject(str, LocalDateTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_ascii() {
        for (LocalDateTime dateTime : dateTimes) {
            LocalDateTime1 vo = new LocalDateTime1();
            vo.setDate(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            LocalDateTime1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, LocalDateTime1.class);
            assertEquals(vo.getDate(), v1.getDate());
        }
    }

    @Test
    public void test_str_1() {
        String str = "{\"date\":\"2021年2月3日\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(3, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_2() {
        String str = "{\"date\":\"2021年12月1日\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_3() {
        String str = "{\"date\":\"2021年12月11日\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_3_h() {
        String str = "{\"date\":\"2021년12월11일\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_1() {
        String str = "{\"date\":\"2021年12月1日\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_1_h() {
        String str = "{\"date\":\"2021년12월1일\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_2() {
        String str = "{\"date\":\"2021年1月21日\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(21, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_2_h() {
        String str = "{\"date\":\"2021년1월21일\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(21, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_3() {
        String str = "{\"date\":\"2021-12-11\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_3_utf8() {
        String str = "{\"date\":\"2021-12-11\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_4() {
        String str = "{\"date\":\"2021/12/11\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_4_utf8() {
        String str = "{\"date\":\"2021/12/11\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_5() {
        String str = "{\"date\":\"11.12.2021\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_5_utf8() {
        String str = "{\"date\":\"11.12.2021\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_6() {
        String str = "{\"date\":\"11-12-2021\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_10_6_utf8() {
        String str = "{\"date\":\"11-12-2021\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_5() {
        String str = "{\"date\":\"20211211\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_5_utf8() {
        String str = "{\"date\":\"20211211\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6() {
        String str = "{\"date\":\"2021-2-1\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_6_utf8() {
        String str = "{\"date\":\"2021-2-1\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(2, vo.getDate().getMonthValue());
        assertEquals(1, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_16_0() {
        String str = "{\"date\":\"2021-12-13 12:13\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(13, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(0, vo.getDate().getSecond());
    }

    @Test
    public void test_str_16_0_utf8() {
        String str = "{\"date\":\"2021-12-13 12:13\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(13, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(0, vo.getDate().getSecond());
    }

    @Test
    public void test_str_17_0() {
        String str = "{\"date\":\"2021-1-2 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(2, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_17_0_utf8() {
        String str = "{\"date\":\"2021-1-2 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(2, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_17_1() {
        String str = "{\"date\":\"2021-12-13T12:13Z\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(13, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(0, vo.getDate().getSecond());
    }

    @Test
    public void test_str_17_1_utf8() {
        String str = "{\"date\":\"2021-12-13T12:13Z\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(13, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(0, vo.getDate().getSecond());
    }

    @Test
    public void test_str_18_0() {
        String str = "{\"date\":\"2021-1-11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_18_0_utf8() {
        String str = "{\"date\":\"2021-1-11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(1, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_18_1() {
        String str = "{\"date\":\"2021-12-3 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(3, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_18_1_utf8() {
        String str = "{\"date\":\"2021-12-3 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(3, vo.getDate().getDayOfMonth());

        assertEquals(12, vo.getDate().getHour());
        assertEquals(13, vo.getDate().getMinute());
        assertEquals(14, vo.getDate().getSecond());
    }

    @Test
    public void test_str_19() {
        String str = "{\"date\":\"2021-12-11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_19_utf8() {
        String str = "{\"date\":\"2021-12-11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_19_1() {
        String str = "{\"date\":\"2021/12/11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str, LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }

    @Test
    public void test_str_19_1_utf8() {
        String str = "{\"date\":\"2021/12/11 12:13:14\"}";
        LocalDateTime1 vo = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), LocalDateTime1.class);
        assertEquals(2021, vo.getDate().getYear());
        assertEquals(12, vo.getDate().getMonthValue());
        assertEquals(11, vo.getDate().getDayOfMonth());
    }
}
