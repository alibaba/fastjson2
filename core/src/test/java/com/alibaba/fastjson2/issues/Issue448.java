package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import java.util.ArrayList;

public class Issue448 {

    @Test
    public void test() {
        BasicStaff staff = prepare();

        // 序列化
        String jsonString = JSON.toJSONString(staff, JSONWriter.Feature.WriteClassName);
        System.out.println(jsonString);

        // 反序列化（就在这出的问题）
        BasicStaff staffD = (BasicStaff) JSON.parseObject(jsonString, Object.class, JSONReader.Feature.SupportAutoType);
        System.out.println(staffD);
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

   public class BasicStaff implements Serializable {
       private List<BasicStaff> boyList;

       @JSONField(format = "yyyyMMdd")
       private LocalDate localDate;

       @JSONField(format = "yyyyMMdd HH:mm:ss")
       private LocalDateTime localDateTime;

       public List<BasicStaff> getBoyList() {
           return boyList;
       }

       public void setBoyList(List<BasicStaff> boyList) {
           this.boyList = boyList;
       }

       public LocalDate getLocalDate() {
           return localDate;
       }

       public void setLocalDate(LocalDate localDate) {
           this.localDate = localDate;
       }

       public LocalDateTime getLocalDateTime() {
           return localDateTime;
       }

       public void setLocalDateTime(LocalDateTime localDateTime) {
           this.localDateTime = localDateTime;
       }

       @Override
       public String toString() {
           return "BasicStaff{" +
                   "boyList=" + boyList +
                   ", localDate=" + localDate +
                   ", localDateTime=" + localDateTime +
                   '}';
       }
   }
}
