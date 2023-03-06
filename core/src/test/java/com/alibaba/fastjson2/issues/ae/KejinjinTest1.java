package com.alibaba.fastjson2.issues.ae;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KejinjinTest1 {
    @Test
    public void testSeeAlso() {
        {
            Base object = JSON.parseObject("{}", Base.class);
            assertTrue(object instanceof Base); // OK
        }
        {
            Base object = JSON.parseObject("{\"type\": \"Extended\"}", Base.class);
            assertTrue(object instanceof Extended); // OK
        }
        {
            Middle1 object = JSON.parseObject("{}", Middle1.class);
            assertTrue(object instanceof Middle1); // OK
        }
        {
            Middle1 object = JSON.parseObject("{\"type\": \"Extended\"}", Middle1.class);
            assertTrue(object instanceof Extended1); // Fail
        }
    }

    @JSONType(seeAlso = Extended.class, typeKey = "type")
    public static class Base {
    }

    @JSONType(typeName = "Extended")
    public static class Extended
            extends Base {
    }

    @JSONType(seeAlso = Extended1.class, typeKey = "type")
    public static class Base1 {
    }

    public static class Middle1
            extends Base1 {
    }

    @JSONType(typeName = "Extended")
    public static class Extended1
            extends Middle1 {
    }
}
