package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;

public class CustomListTest {
    public static class MyDTO {
        private MyList myList;

        public MyList getMyList() {
            return myList;
        }

        public void setMyList(MyList myList) {
            this.myList = myList;
        }
    }

    public static class MyList extends ArrayList<MyObject> {
    }

    public static class MyObject {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        String json = "{\"myList\":[{\"name\":\"张三\"},{\"name\":\"李四\"}]}";
        MyDTO myDTO = JSON.parseObject(json, MyDTO.class);

        System.out.println("MyList size: " + myDTO.getMyList().size());

        for (int i = 0; i < myDTO.getMyList().size(); i++) {
            Object obj = myDTO.getMyList().get(i);
            System.out.println("Element " + i + " type: " + obj.getClass().getName());

            if (obj instanceof MyObject) {
                MyObject myObject = (MyObject) obj;
                System.out.println("MyObject name: " + myObject.getName());
            } else if (obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;
                System.out.println("JSONObject: " + jsonObject.toJSONString());
            }
        }

        MyObject myObject = myDTO.getMyList().get(0);
        System.out.println("Direct access result: " + myObject.getName());
    }
}
