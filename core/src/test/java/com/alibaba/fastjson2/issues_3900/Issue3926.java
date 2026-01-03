package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3926 {
    public static class MyDTO1 {
        private MyList myList;

        public MyList getMyList() {
            return myList;
        }

        public void setMyList(MyList myList) {
            this.myList = myList;
        }
    }

    public static class MyList extends ArrayList<MyObject1> {
    }

    public static class MyObject1 {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testCustomList() {
        String json = "{\"myList\":[{\"name\":\"张三\"},{\"name\":\"李四\"}]}";
        MyDTO1 myDTO = JSON.parseObject(json, MyDTO1.class);

        assertEquals(2, myDTO.getMyList().size());

        for (int i = 0; i < myDTO.getMyList().size(); i++) {
            Object obj = myDTO.getMyList().get(i);
            assertTrue(obj != null, "Element " + i + " should be MyObject, but is " + obj.getClass().getName());
        }

        MyObject1 myObject = myDTO.getMyList().get(0);
        assertEquals("张三", myObject.getName());

        MyObject1 myObject2 = myDTO.getMyList().get(1);
        assertEquals("李四", myObject2.getName());
    }

    public static class MyDTO2 {
        private MyList2 myList;

        public MyList2 getMyList() {
            return myList;
        }

        public void setMyList(MyList2 myList) {
            this.myList = myList;
        }
    }

    public interface MyListInterface extends java.util.List<MyObject2> {
    }

    public static class MyList2 extends ArrayList<MyObject2> implements MyListInterface {
    }

    public static class MyObject2 {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testCustomListWithInterfaceSingle() {
        String json = "{\"myList\":[{\"name\":\"张三\"}]}";
        MyDTO2 myDTO = JSON.parseObject(json, MyDTO2.class);

        assertEquals(1, myDTO.getMyList().size());

        Object obj = myDTO.getMyList().get(0);
        assertTrue(obj != null, "Element should be MyObject2, but is " + obj.getClass().getName());

        MyObject2 myObject = myDTO.getMyList().get(0);
        assertEquals("张三", myObject.getName());
    }

    @Test
    public void testCustomListWithInterface() {
        String json = "{\"myList\":[{\"name\":\"张三\"},{\"name\":\"李四\"}]}";
        MyDTO2 myDTO = JSON.parseObject(json, MyDTO2.class);

        assertEquals(2, myDTO.getMyList().size());

        for (int i = 0; i < myDTO.getMyList().size(); i++) {
            Object obj = myDTO.getMyList().get(i);
            assertTrue(obj instanceof MyObject2, "Element " + i + " should be MyObject2, but is " + obj.getClass().getName());
        }

        MyObject2 myObject = myDTO.getMyList().get(0);
        assertEquals("张三", myObject.getName());

        MyObject2 myObject2 = myDTO.getMyList().get(1);
        assertEquals("李四", myObject2.getName());
    }
}
