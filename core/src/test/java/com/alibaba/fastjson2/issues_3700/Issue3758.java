package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * Test for issue #3758
 * Date format compatibility between fastjson1 and fastjson2
 */
public class Issue3758 {
    /**
     * Test case 1: yyyy-MM-dd format with datetime string.
     * In fastjson1: 2023-10-11 11:25:10 -> 2023-10-11 00:00:00
     * In fastjson2: 2023-10-11 11:25:10 -> 2023-10-11 11:25:10 (incompatible)
     */
    @Test
    public void testYMDFormatWithDateTimeString() {
        String json = "{\"utilDate\":\"2023-10-11 11:25:10\"}";
        TestDTO1 dto = JSON.parseObject(json, TestDTO1.class);

        // Expected behavior: time part should be 00:00:00 for yyyy-MM-dd format
        // But currently fastjson2 keeps the time part
        System.out.println("Parsed date: " + dto.utilDate);
        System.out.println("Formatted date: " + JSON.toJSONString(dto));
    }

    /**
     * Test case 2: HH:mm and HH:mm:ss format.
     * In fastjson1: 11:25:10 -> Thu Jan 01 11:25:10 CST 1970
     * In fastjson2: 11:25:10 -> Sun Jul 05 16:11:10 CST 169108099 (incompatible)
     */
    @Test
    public void testTimeFormat() {
        String json = "{\"utilTime\":\"11:25:10\"}";
        TestDTO2 dto = JSON.parseObject(json, TestDTO2.class);

        // Expected behavior: date part should be 1970-01-01 for time-only format
        // But currently fastjson2 uses LocalDate.MIN
        System.out.println("Parsed time: " + dto.utilTime);
        System.out.println("Formatted time: " + JSON.toJSONString(dto));
    }

    /**
     * Test case 3: HH:mm format.
     */
    @Test
    public void testHHmmFormat() {
        String json = "{\"utilTime\":\"11:25\"}";
        TestDTO3 dto = JSON.parseObject(json, TestDTO3.class);

        System.out.println("Parsed time: " + dto.utilTime);
        System.out.println("Formatted time: " + JSON.toJSONString(dto));
    }

    /**
     * Test case 4: yyyy-MM-dd format with date only string.
     */
    @Test
    public void testYMDFormatWithDateString() {
        String json = "{\"utilDate\":\"2023-10-11\"}";
        TestDTO1 dto = JSON.parseObject(json, TestDTO1.class);

        System.out.println("Parsed date: " + dto.utilDate);
        System.out.println("Formatted date: " + JSON.toJSONString(dto));
    }

    @Data
    public static class TestDTO1 {
        @JSONField(format = "yyyy-MM-dd")
        private Date utilDate;
    }

    @Data
    public static class TestDTO2 {
        @JSONField(format = "HH:mm:ss")
        private Date utilTime;
    }

    @Data
    public static class TestDTO3 {
        @JSONField(format = "HH:mm")
        private Date utilTime;
    }
}
