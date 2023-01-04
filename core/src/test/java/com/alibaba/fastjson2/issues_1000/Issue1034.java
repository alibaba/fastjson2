package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1034 {
    @Test
    public void test() {
        String json = "{\"PARAMETER\":{\"SSRC\":[null,15,null,null,null,null,null,null,null,null,null,null,null],\"RUN\":124}}";
        Param<?> param = JSON.parseObject(json, Param.class);
        JSONObject jsonBody = (JSONObject) param.getParameter();
        TypeReference<Bean> typeReference = new TypeReference<Bean>() {};
        Object to = jsonBody.to(typeReference.getType());
        assertEquals("{\"RUN\":124,\"SSRC\":[null,15,null,null,null,null,null,null,null,null,null,null,null]}", JSON.toJSONString(to));
    }

    @Getter
    @Setter
    static class Bean {
        @JSONField(name = "RUN")
        private Integer run;
        @JSONField(name = "SSRC")
        private List<Integer> ssrc;
    }

    @Getter
    @Setter
    static class Param<T> {
        @JSONField(name = "PARAMETER")
        private T parameter;
    }
}
