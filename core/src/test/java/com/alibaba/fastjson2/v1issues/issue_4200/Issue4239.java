package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.NameFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4239 {
    @Test
    public void test() {
        JSONEntity entity = JSONEntity.builder()
                .id(1)
                .name("joker")
                .pwd("123@321")
                .time(LocalDateTime.of(2022, 11, 12, 0, 0, 0))
                .time2(LocalDateTime.of(2022, 11, 12, 0, 0, 0))
                .build();

        String beanToJsonStr = JSON.toJSONString(entity, NameFilter.of(PropertyNamingStrategy.UpperCase));
        assertEquals(
                "{\"ID\":1,\"NAME\":\"joker\",\"PWD\":\"123@321\",\"TIME\":\"20221112\",\"TIME2\":\"20221112\"}",
                beanToJsonStr
        );
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JSONEntity {
        private Integer id;
        private String name;
        private String pwd;

        @JSONField(format = "yyyyMMdd")
        private LocalDateTime time;

        @JSONField(format = "yyyyMMdd")
        private LocalDateTime time2;

        private UserEntity userEntity;

        private List<StudentEntity> studentEntityList;
    }

    public static class UserEntity {
    }

    public static class StudentEntity {
    }
}
