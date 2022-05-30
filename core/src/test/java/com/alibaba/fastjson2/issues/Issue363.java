package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue363 {
    @Test
    public void test() {
        ProgramGridOutput vo = new ProgramGridOutput();
        vo.validDate = LocalDate.of(2022, 5, 27);
        String str = JSON.toJSONString(vo, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"validDate\":\"2022-05-27\"}", str);

        ProgramGridOutput vo2 = JSON.parseObject(str, ProgramGridOutput.class);
        assertEquals(vo.validDate, vo2.validDate);

        ProgramGridOutput vo3 = JSON.parseObject("{\"validDate\":\"2022-05-27\"}", ProgramGridOutput.class, "yyyy-MM-dd HH:mm:ss");
        assertEquals(vo.validDate, vo3.validDate);
    }

    @Data
    public class ProgramGridOutput {
        private Long id;
        private Long attachmentId;
        private String name;
        @JSONField(format = "yyyy-MM-dd")
        private LocalDate entryDate;
        @JSONField(format = "yyyy-MM-dd")
        private LocalDate validDate;
        private LocalDateTime createTime;
    }
}
