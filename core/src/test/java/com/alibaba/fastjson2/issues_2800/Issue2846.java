package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2846 {
    @Test
    public void testFastJson() throws Exception {
        CombineView combineView = new CombineView();
        String jacksonResult = new ObjectMapper().writeValueAsString(combineView);
        String fastjsonResult = JSON.toJSONString(new CombineView());
        assertEquals(jacksonResult, fastjsonResult);
    }

    static class CombineView {
        @JsonUnwrapped
        public View1 author() {
            return new View1();
        }
    }

    static class View1 {
        public String getAuthorName() {
            return "name";
        }
    }
}
