package com.alibaba.fastjson2.v1issues.issue_4300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue4365 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"address\":\"1\",\"admin\":true}";
        Info info = JSON.parseObject(json, Info.class);
        assertEquals("1", info.getAddress());
        assertTrue(info.isAdmin());
    }

    public class Info {
        private String address;
        private boolean admin;

        public Info(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }
    }
}
