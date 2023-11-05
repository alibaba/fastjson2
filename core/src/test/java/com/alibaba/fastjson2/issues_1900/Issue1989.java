package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1989 {
    @Test
    public void test() {
        String paramStr = "{\n\"a\": \"\",\n\"b\": \"#b#\",\n\"d\": \"c\",\n\"d\": {\n\"f\": \"#f#\",\n\"g\": \"#g#\",\n\"h\": [\n    \"j\":[] \n]\n}\n}";
        assertThrows(
                JSONException.class,
                () -> JSON.parse(paramStr));
    }
}
