package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue274 {
    @Test
    public void test() {
        Bean bean = JSON
                .parseObject("{\"roleList\": [\"1\"]}")
                .to(Bean.class);
        assertEquals("1", bean.roleList.get(0));

        Bean bean1 = JSON
                .parseObject("{\"roleList\": [1,null]}")
                .to(Bean.class);
        assertEquals("1", bean1.roleList.get(0));
        assertEquals(null, bean1.roleList.get(1));

        Bean bean2 = JSON
                .parseObject("{\"roleList\": [\"1\",null]}")
                .to(Bean.class);
        assertEquals("1", bean2.roleList.get(0));
        assertEquals(null, bean2.roleList.get(1));

        Bean bean3 = JSON
                .parseObject("{\"roleList\": [1,\"2\"]}")
                .to(Bean.class);
        assertEquals("1", bean3.roleList.get(0));
        assertEquals("2", bean3.roleList.get(1));
    }

    public static class Bean {
        private List<String> roleList;

        public List<String> getRoleList() {
            return roleList;
        }

        public void setRoleList(List<String> roleList) {
            this.roleList = roleList;
        }
    }
}
