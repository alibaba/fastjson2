package com.alibaba.fastjson2.v1issues.issue_4000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4073 {
    //CS304 (manually written) Issue link: https://github.com/alibaba/fastjson/issues/4073
    @Test
    public void test1() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aBc", "abc");
        jsonObject.put("i_d_d", "123456");
        jsonObject.put("create_date", "2022-04-24 00:00:00");
        User user = jsonObject.toJavaObject(User.class);
        assertEquals("User{aBc='abc', ID='123456', date=Sun Apr 24 00:00:00 CST 2022}", user.toString());

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("aBc", "abc");
        jsonObject1.put("ID", "123456");
        jsonObject1.put("date", "2022-04-24 00:00:00");
        User user1 = jsonObject1.toJavaObject(User.class);
        assertEquals("User{aBc='abc', ID='null', date=null}", user1.toString());
    }

    public static class User {
        private String aBc;
        @JSONField(name = "i_d_d")
        private String ID;
        @JSONField(name = "create_date")
        private Date date;

        public String getaBc() {
            return aBc;
        }

        public void setaBc(String aBc) {
            this.aBc = aBc;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "User{" +
                    "aBc='" + aBc + '\'' +
                    ", ID='" + ID + '\'' +
                    ", date=" + date +
                    '}';
        }
    }
}
