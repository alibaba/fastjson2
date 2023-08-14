package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kraity
 */
public class Issue1724 {
    @Test
    public void test() {
        MyObj obj = new MyObj(new Exception("异常信息2", new Exception("异常信息1")));
        String json = JSON.toJSONString(obj, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        assertNotNull(json);
        {
            MyObj myObj = assertDoesNotThrow(() -> JSON.parseObject(json, MyObj.class, JSONReader.Feature.SupportAutoType));
            assertNotNull(myObj);
            assertNotNull(myObj.throwable);
            assertEquals("异常信息2", myObj.throwable.getMessage());
            assertNotNull(myObj.throwable.getCause());
            assertEquals("异常信息1", myObj.throwable.getCause().getMessage());
        }

        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        {
            MyObj myObj = assertDoesNotThrow(() -> JSON.parseObject(jsonBytes, MyObj.class, JSONReader.Feature.SupportAutoType));
            assertNotNull(myObj);
            assertNotNull(myObj.throwable);
            assertEquals("异常信息2", myObj.throwable.getMessage());
            assertNotNull(myObj.throwable.getCause());
            assertEquals("异常信息1", myObj.throwable.getCause().getMessage());
        }
    }

    public static class MyObj {
        private Throwable throwable;

        public MyObj(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
