package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1153 {
    @Test
    public void test() {
        JSONPath path = JSONPath.of("$[-1].fullName");

        String json = " [{\"age\":11,\"dateOfBirth\":\"2023-02-27 17:49:15\",\"fullName\":\"Tide\"}," +
                "{\"age\":81,\"dateOfBirth\":\"1976-09-10 11:17:36\",\"fullName\":\"Bob\"}," +
                "{\"age\":52,\"dateOfBirth\":\"1998-11-16 07:44:16\",\"fullName\":\"Sony\"}]";

        /*
            $.[0].fullName - Tide
            [0].fullName - Tide
            [1].fullName - Bob
            [-1].fullName - Tide ???
         */
        assertEquals(
                "Sony",
                JSONPath.of("$[-1].fullName")
                        .extract(
                                JSONReader.of(json)
                        )
        );

        assertEquals(
                "Sony",
                ((JSONObject) JSONPath.of("$[-1]")
                        .extract(
                                JSONReader.of(json)
                        )
                ).getString("fullName")
        );
    }
}
