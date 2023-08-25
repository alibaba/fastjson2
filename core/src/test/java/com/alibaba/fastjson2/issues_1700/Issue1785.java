package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class Issue1785 {
    @Test
    public void test() throws Exception {
        int count = RecruitWorkSource.createStat.get();

        String jsonString = JSON.toJSONString(RecruitWorkSource.work_Online);
        RecruitWorkSource enumsTestDTO1 = JSON.parseObject(jsonString, RecruitWorkSource.class);
        assertEquals(count + 1, RecruitWorkSource.createStat.get());
        RecruitWorkSource enumsTestDTO2 = JSON.parseObject(jsonString, new TypeReference<RecruitWorkSource>() {});
        assertEquals(count + 2, RecruitWorkSource.createStat.get());
    }

    @Getter
    @AllArgsConstructor
    public enum RecruitWorkSource implements BaseEnum<RecruitWorkSource, String> {
        work_Online("NQJQXLJH", "dfsdfsdf", ""),
        Building_Harbour("T59GKNQJQXLJHMYY", "", ""),
        other("other", "其它", "");

        @JsonValue
        private final String value;
        //描述
        private final String description;
        //推送地址
        private final String pushUrl;

        static final AtomicInteger createStat = new AtomicInteger();

        @JsonCreator
        public static RecruitWorkSource getItem(String code) {
            createStat.incrementAndGet();

            switch (code) {
                case "NQJQXLJH":
                    return work_Online;
                case "T59GKNQJQXLJHMYY":
                    return Building_Harbour;
                case "other":
                    return other;
            }
            return null;
        }
    }

    public interface BaseEnum<K, V> {
    }
}
