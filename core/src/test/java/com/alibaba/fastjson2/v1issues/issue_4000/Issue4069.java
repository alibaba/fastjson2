package com.alibaba.fastjson2.v1issues.issue_4000;

import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue4069 {
    @Test
    public void test_for_issue4069() {
        String json_string = "[[{\"value\": \"aaa\",\"key\": \"input_aKLYFkHNhPk0\"},{\"value\": \"222\",\"key\": \"number_pPvGTENKofUM\"}]," +
                "[{\"value\": \"ffdf\",\"key\": \"input_aKLYFkHNhPk0\"},{\"value\": \"1212\",\"key\": \"number_pPvGTENKofUM\"}]]";

        assertTrue(JSON.isValidArray(json_string));
        assertEquals("Array", JSONValidator.from(json_string).getType().toString());

        assertTrue(JSON.isValidArray("[[]]"));
        assertEquals("Array", JSONValidator.from("[[]]").getType().toString());

        assertFalse(JSON.isValidArray("["));
        assertFalse(JSON.isValidArray("[[]"));
    }
}
