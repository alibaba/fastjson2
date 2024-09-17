package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2765 {
    @Test
    public void test1() {
        JSONObject object = new JSONObject();
        object.put("status", Status.ACTIVE);

        String jsonStr = JSON.toJSONString(object, JSONWriter.Feature.WriteEnumUsingOrdinal);
        assertEquals("{\"status\":0}", jsonStr);
    }

    @Test
    public void test2() {
        VO vo = new VO();
        vo.setStatus(Status.ACTIVE);

        String serializedObject = JSON.toJSONString(vo, JSONWriter.Feature.WriteEnumUsingOrdinal);
        assertEquals("{\"status\":0}", serializedObject);
    }

    @Data
    public static class VO {
        private Status status;
    }

    enum Status {
        ACTIVE,
        INACTIVE
    }
}
