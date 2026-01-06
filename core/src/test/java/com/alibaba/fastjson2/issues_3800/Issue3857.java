package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3857 {
    @Test
    public void testYearVariants() {
        Year parsed = JSON.parseObject("\"+2023\"", Year.class);
        assertEquals(Year.of(2023), parsed);

        parsed = JSON.parseObject("2023", Year.class);
        assertEquals(Year.of(2023), parsed);
    }

    @Test
    public void testYearMonthVariants() {
        YearMonth parsed = JSON.parseObject("\"202311\"", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);

        parsed = JSON.parseObject("\"+2023-11\"", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);

        parsed = JSON.parseObject("202311", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);
    }

    @Test
    public void testMonthDayVariants() {
        MonthDay parsed = JSON.parseObject("\"12-25\"", MonthDay.class);
        assertEquals(MonthDay.of(12, 25), parsed);

        parsed = JSON.parseObject("1225", MonthDay.class);
        assertEquals(MonthDay.of(12, 25), parsed);
    }

    @Test
    public void testChronoRoundTrip() {
        ChronoBean bean = new ChronoBean();
        bean.hijrahDate = HijrahDate.now();
        bean.japaneseDate = JapaneseDate.now();
        bean.minguoDate = MinguoDate.now();
        bean.thaiBuddhistDate = ThaiBuddhistDate.now();

        String json = JSON.toJSONString(bean);
        ChronoBean parsed = JSON.parseObject(json, ChronoBean.class);

        assertEquals(bean.hijrahDate, parsed.hijrahDate);
        assertEquals(bean.japaneseDate, parsed.japaneseDate);
        assertEquals(bean.minguoDate, parsed.minguoDate);
        assertEquals(bean.thaiBuddhistDate, parsed.thaiBuddhistDate);
    }

    static class ChronoBean {
        public HijrahDate hijrahDate;
        public JapaneseDate japaneseDate;
        public MinguoDate minguoDate;
        public ThaiBuddhistDate thaiBuddhistDate;
    }
}
