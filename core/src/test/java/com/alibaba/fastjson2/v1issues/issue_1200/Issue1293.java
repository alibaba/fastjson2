package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by kimmking on 27/06/2017.
 */
public class Issue1293 {
    @Test
    public void test_for_issue() {
        String data = "{\"idType\":\"123123\",\"userType\":\"134\",\"count\":\"123123\"}";
        {
            Bean test = JSON.parseObject(data, Bean.class);

            assertNull(test.idType);
            assertNull(test.userType);
        }

        Bean test = JSON.parseObject(data, Bean.class);
        assertNull(test.idType);
        assertNull(test.userType);
    }

    static class Bean {
        private long count;
        private IdType idType;
        private UserType userType;

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public IdType getIdType() {
            return idType;
        }

        public void setIdType(IdType idType) {
            this.idType = idType;
        }

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
        }
    }

    static enum IdType {
        A, B
    }

    static enum UserType {
        C, D
    }
}
