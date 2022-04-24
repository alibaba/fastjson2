package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2229 {
    @Test
    public void test_for_issue() throws Exception {
        Jon jon = JSON.parseObject("{\"dStr\":\"         hahahaha        \",\"user\":{\"createtime\":null,\"id\":0,\"username\":\"  asdfsadf  asdf  asdf  \"}}", Jon.class);
        assertEquals("  asdfsadf  asdf  asdf  ", jon.user.username);
    }

    @Test
    public void test_for_issue1() throws Exception {
        Jon jon1 = JSON.parseObject("{'dStr':'         hahahaha        ','user':{'createtime':null,'id':0,'username':'  asdfsadf  asdf  asdf  '}}", Jon.class);
        assertEquals("  asdfsadf  asdf  asdf  ", jon1.user.username);
    }

    public static class Jon {
        public String dStr;
        public User user;
    }

    public static class User {
        public int id;
        public Date createtime;
        public String username;
    }
}
