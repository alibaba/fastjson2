package com.alibaba.fastjson2.v1issues.issue_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue_20201016_02 {
    @Test
    public void testFastJson() {
        String s = "{\"agent\":null,\"creator\":{\"account\":\"account\",\"name\":\"name\",\"workid\":null},\"owner\":{\"$ref\":\"$.creator\"}}";

        Config config = JSON.parseObject(s, Config.class);
        assertNotNull(config.creator);
        assertNull(config.agent);
    }

    public static class Config {
        private UserConfig creator;
        private UserConfig agent;

        public UserConfig getCreator() {
            return creator;
        }

        public void setCreator(UserConfig creator) {
            this.creator = creator;
        }

        public UserConfig getAgent() {
            return agent;
        }

        public void setAgent(UserConfig agent) {
            this.agent = agent;
        }
    }

    public static class UserConfig {
        private String name;
        private String account;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
    }
}
