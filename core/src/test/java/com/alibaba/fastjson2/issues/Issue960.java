package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue960 {
    @Test
    public void test() {
        String jsonStr = "[{\"phone\":\"电话1\",\"username\":\"姓名1\",\"clinictype\":\"副高号\",\"doctorname\":\"李真\",\"doctorcode\":\"SJK11\",\"deptname\":\"科室1\",\"deptcode\":\"3003\"},{\"phone\":\"电话2\",\"username\":\"姓名2\",\"clinictype\":\"副高号\",\"doctorname\":\"李真\",\"doctorcode\":\"SJK11\",\"deptname\":\"科室2\",\"deptcode\":\"3004\"}]";
        List<UserInfo> list = JSON.parseArray(jsonStr, UserInfo.class, JSONReader.Feature.SupportSmartMatch);
        list.forEach(
                userInfo -> {
                    assertNotNull(userInfo.userName);
                    assertNotNull(userInfo.phone);
                    assertNotNull(userInfo.clinicType);
                    assertNotNull(userInfo.deptCode);
                    assertNotNull(userInfo.deptName);
                    assertNotNull(userInfo.doctorCode);
                    assertNotNull(userInfo.doctorName);
                }
        );
    }

    @Data
    public static class UserInfo {
        private String userName;
        private String phone;
        private String clinicType;
        private String deptCode;
        private String deptName;
        private String doctorCode;
        private String doctorName;
    }
}
