package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1822 {
    private static String JSON_STRING;
    private static MyObj MY_OBJ;

    @BeforeAll
    public static void init() {
        MY_OBJ = new MyObj();
        MY_OBJ.setThrowable(new Throwable("测试"));
        JSON_STRING = JSON.toJSONString(MY_OBJ);
    }

    @Test
    void testWithError() {
        JSONObject jsonObject = JSONObject.parseObject(JSON_STRING);
        MyObj myObj = jsonObject.toJavaObject(MyObj.class);
        assertEquals(JSON_STRING, JSON.toJSONString(myObj));
        assertEquals(MY_OBJ.toString(), myObj.toString());
    }

    @Test
    void testWithoutError() {
        MyObj myObj = JSONObject.parseObject(JSON_STRING, MyObj.class);
        assertEquals(JSON_STRING, JSON.toJSONString(myObj));
        assertEquals(MY_OBJ.toString(), myObj.toString());
    }

    private static class MyObj {
        private Throwable throwable;

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public String toString() {
            return "MyObj{" +
                    "throwable=" + throwable +
                    '}';
        }
    }
}
