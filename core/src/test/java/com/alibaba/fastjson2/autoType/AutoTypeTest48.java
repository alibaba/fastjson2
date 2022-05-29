package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AutoTypeTest48 {
    @Test
    public void test_0() throws Exception {
        assertThrows(JSONException.class, () -> {
                    JSON.parse((String) JSONB
                            .parse(Base64.getDecoder()
                                    .decode("eThueyJAdHlwZSI6Iltjb20uc3VuLnJvd3NldC5KZGJjUm93U2V0SW1wbCIsWyJkYXRhU291cmNlTmFtZSI6ImxkYXA6Ly8xMjcuMC4wLjE6MTM4OS9qcnRmbnkiLCJhdXRvQ29tbWl0Ijp0cnVlXX0=")
                            ), JSONReader.Feature.SupportAutoType
                    );
                }
        );
    }
}
