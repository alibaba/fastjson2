package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2632 {
    @Test
    public void test() {
        String jsn = "{\n" +
                "  \"errcode\": 0,\n" +
                "  \"errmsg\": \"ok\",\n" +
                "  \"result\": {\n" +
                "    \"active\": true,\n" +
                "    \"admin\": false,\n" +
                "    \"avatar\": \"\",\n" +
                "    \"boss\": false,\n" +
                "    \"create_time\": \"2024-05-14T08:05:45.000Z\",\n" +
                "    \"dept_id_list\": [\n" +
                "      111111111\n" +
                "    ],\n" +
                "    \"dept_order_list\": [\n" +
                "      {\n" +
                "        \"dept_id\": 111111111,\n" +
                "        \"order\": 1111111111111\n" +
                "      }\n" +
                "    ],\n" +
                "    \"email\": \"\",\n" +
                "    \"exclusive_account\": false,\n" +
                "    \"hide_mobile\": false,\n" +
                "    \"leader_in_dept\": [\n" +
                "      {\n" +
                "        \"dept_id\": 862258146,\n" +
                "        \"leader\": false\n" +
                "      }\n" +
                "    ],\n" +
                "    \"mobile\": \"11111111111\",\n" +
                "    \"name\": \"张三\",\n" +
                "    \"real_authed\": false,\n" +
                "    \"remark\": \"\",\n" +
                "    \"senior\": false,\n" +
                "    \"state_code\": \"86\",\n" +
                "    \"telephone\": \"\",\n" +
                "    \"title\": \"开发部\",\n" +
                "    \"unionid\": \"IKKo8dfdfdfdfiSfn2nJEOwiEiE\",\n" +
                "    \"userid\": \"zhangsan\",\n" +
                "    \"work_place\": \"\"\n" +
                "  },\n" +
                "  \"request_id\": \"16khdfdfsdfkv560dw\"\n" +
                "}";
        JSONObject o = JSON.parseObject(jsn);
        JSONObject result = o.getJSONObject("result");
        DDUserInfo javaObject = result.toJavaObject(DDUserInfo.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(862258146, javaObject.leaderInDept.get(0).deptId);
    }

    @Data
    public static class DDUserInfo {
        private String extension;
        private String unionid;
        private String boss;
        private List<Role> roleList;
        private boolean exclusiveAccount;
        private String managerUserid;
        private String admin;
        private String remark;
        private String title;
        private String hiredDate;
        private String userid;
        private String workPlace;
        private List<DeptOrder> deptOrderList;
        private String realAuthed;
        private String deptIdList;
        private String deptName;
        private String jobNumber;
        private String email;
        private List<LeaderInDept> leaderInDept;
        private String mobile;
        private String active;
        private String orgEmail;
        private String telephone;
        private String avatar;
        private String hideMobile;
        private String senior;
        private String name;
        private String stateCode;
    }

    @Data
    public static class LeaderInDept {
        private String leader;
        private Long deptId;

        public LeaderInDept() {
        }
    }

    @Data
    public static class DeptOrder {
        private Long deptId;
        private String order;
    }

    @Data
    public static class Role {
        private String groupName;
        private String name;
        private Long id;
    }
}
