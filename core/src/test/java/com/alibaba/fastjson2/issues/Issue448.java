package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Issue448 {
    @Data
    public class BasicStaff
            implements Serializable {
        // 小弟列表
        private List<BasicStaff> boyList;

        @JSONField(format = "yyyyMMdd")
        private LocalDate localDate;

        @JSONField(format = "yyyyMMdd HH:mm:ss")
        private LocalDateTime localDateTime;
    }

    private BasicStaff prepare() {
        BasicStaff staff = new BasicStaff();
        staff.setLocalDate(LocalDate.now());
        staff.setLocalDateTime(LocalDateTime.now());

        List<BasicStaff> list = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            BasicStaff staffI = new BasicStaff();

            staffI.setLocalDate(LocalDate.now());
            staffI.setLocalDateTime(LocalDateTime.now());

            list.add(staffI);
        }
        //  嵌套同样类型的bean，就会导致OOM
        staff.setBoyList(list);

        return staff;
    }

    @Test
    public void test() {
        BasicStaff staff = prepare();

        // 序列化
        String jsonString = JSON.toJSONString(staff, JSONWriter.Feature.WriteClassName);

        // 反序列化（就在这出的问题）
        BasicStaff staffD = (BasicStaff) JSON.parseObject(jsonString, Object.class, JSONReader.Feature.SupportAutoType);
        System.out.println(staffD);
    }
}
