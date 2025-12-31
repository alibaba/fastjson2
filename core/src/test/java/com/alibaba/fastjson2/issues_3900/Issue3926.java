package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3926 {
    @Test
    public void test() {
        MyDTO myDTO = JSON.parseObject("{\"myList\":[{\"name\":\"张三\"}]}", MyDTO.class);
        MyObject myObject = myDTO.getMyList().get(0);
        assertEquals("张三", myObject.getName());
    }

    public static class MyDTO {
        private MyList myList;

        public MyList getMyList() {
            return myList;
        }

        public void setMyList(MyList myList) {
            this.myList = myList;
        }
    }

    public static class MyList extends ArrayList<MyObject> {}

    public static class MyObject {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
