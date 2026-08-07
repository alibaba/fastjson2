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
    protected void test() {
        ProgramGridOutput vo = new ProgramGridOutput();
        assertEquals("{}", JSON.toJSONString(vo));
        JSON.parseObject("{}", ProgramGridOutput.class);
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
