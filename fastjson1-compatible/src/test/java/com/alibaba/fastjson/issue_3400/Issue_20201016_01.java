package com.alibaba.fastjson.issue_3400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue_20201016_01 {
    @Test
    public void testToString() {
        UserConfig user = new UserConfig();
        user.setAccount("account");
        user.setName("name");

        Config config = new Config();
        config.setCreator(user);
        config.setOwner(user);

        String s = JSON.toJSONString(config, SerializerFeature.WriteMapNullValue,
                SerializerFeature.QuoteFieldNames, SerializerFeature.WriteNullListAsEmpty);

        if ("{\"agent\":null,\"creator\":{\"account\":\"account\",\"name\":\"name\",\"workid\":null},\"owner\":{\"$ref\":\"$.creator\"}}".equals(s)) {
            return;
        }

        assertEquals("{\"agent\":null,\"creator\":{\"account\":\"account\",\"name\":\"name\",\"workid\":null},\"owner\":{\"account\":\"account\",\"name\":\"name\",\"workid\":\"\"}}", s);
    }

    @Test
    public void testFastJson() {
        String s = "{\"agent\":null,\"creator\":{\"account\":\"account\",\"name\":\"name\",\"workid\":null},\"owner\":{\"$ref\":\"$.creator\"}}";

        Config config = JSON.parseObject(s, Config.class);
        assertNotNull(config.creator);
        assertNull(config.agent);
    }

    public static class Config {
        private UserConfig creator;
        private UserConfig owner;
        private UserConfig agent;

        public UserConfig getCreator() {
            return creator;
        }

        public void setCreator(UserConfig creator) {
            this.creator = creator;
        }

        public UserConfig getOwner() {
            return owner;
        }

        public void setOwner(UserConfig owner) {
            this.owner = owner;
        }

        public UserConfig getAgent() {
            return agent;
        }

        public void setAgent(UserConfig agent) {
            this.agent = agent;
        }
    }

    public static class UserConfig {
        private String workid;
        private String name;
        private String account;

        public String getWorkid() {
            return workid;
        }

        public void setWorkid(String workid) {
            this.workid = workid;
        }

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
