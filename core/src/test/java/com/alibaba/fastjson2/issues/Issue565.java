package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue565 {
    @Test
    public void test() {
        String str = "{\"roomType\":\"GRASS_SQUARE_FLOWER\",\"users\":[{\"name\":\"STTEST02\",\"userId\":\"101177\"},{\"name\":\"STTEST01\",\"userId\":\"101176\"}]}";
        Response resp = JSON.parseObject(str, Response.class);
        assertNotNull(resp);
        assertEquals("GRASS_SQUARE_FLOWER", resp.roomType);
    }

    public class Response
            implements Serializable {
        private static final long serialVersionUID = 7868798773717812999L;
        private String roomType;
        private List<AccountInfo> users;

        public class AccountInfo {
            private String userId;
            private String name;

            public AccountInfo() {
            }

            public AccountInfo(String userId, String name) {
                this.userId = userId;
                this.name = name;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                // TODO Auto-generated method stub
                return this.name;
            }
        }

        public String getRoomType() {
            return roomType;
        }

        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }

        public List<AccountInfo> getUsers() {
            return users;
        }

        public void setUsers(List<AccountInfo> users) {
            this.users = users;
        }
    }
}
