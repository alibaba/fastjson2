package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2211 {
    @Test
    public void test() {
        UserDo userDo = new UserDo();
        String json = JSON.toJSONString(userDo, JSONWriter.Feature.WriteNullNumberAsZero, JSONWriter.Feature.WriteLongAsString);
        assertEquals("{\"b1\":true,\"d0\":0,\"d1\":1.0,\"n0\":\"0\",\"n1\":\"1\",\"s1\":\"noear\"}", json);
    }

    @Getter
    @Setter
    public static class UserDo
            implements Serializable {
        String s0;

        String s1 = "noear";

        Boolean b0;
        boolean b1 = true;

        Long n0;
        Long n1 = 1L;

        Double d0;
        Double d1 = 1.0D;

        Object obj0;
        List list0;
        Map map0;
        Map map1;
    }
}
