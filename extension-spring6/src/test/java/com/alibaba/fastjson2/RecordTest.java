package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecordTest {
    @Test
    public void test() {
        String json = """
                {
                    "name" : "x",
                    "age"  : 11,
                    "birthDay" : "1993-02-15"
                }
                """;
        JSONObject jsonObject = JSON.parseObject(json);
        LongParamRecord longParamRecord = jsonObject.to(LongParamRecord.class);
        assertNotNull(longParamRecord.birthDay);
    }

    public record LongParamRecord(String name, Integer age, Date birthDay) {
        public LongParamRecord(String name, Integer age, Date birthDay) {
            this.name = name;
            this.age = age;
            this.birthDay = birthDay;
        }

        public LongParamRecord(String name, Integer age, String date) {
            this(name, age, DateUtils.parseDate(date, "yyyy-MM-dd"));
        }
    }
}
