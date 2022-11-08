package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue904 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of(
                "transactions",
                JSONArray.of(
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of("1a", "2b", "3c")
                                        )
                                )
                        ),
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of("4d", "5e", 6)
                                        )
                                )
                        ),
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of(7.1, 8.2, 9.0)
                                        )
                                )
                        ),
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of(BigDecimal.valueOf(10))
                                        )
                                )
                        ),
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of(BigInteger.valueOf(11))
                                        )
                                )
                        ),
                        JSONObject.of(
                                "transaction",
                                JSONObject.of(
                                        "message",
                                        JSONObject.of(
                                                "accountKeys",
                                                JSONArray.of(Float.valueOf(12))
                                        )
                                )
                        )
                )
        );

        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[\"1a\",\"2b\",\"3c\"]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains('1a'))]"
                ).toString()
        );
        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[\"4d\",\"5e\",6]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains(6))]"
                ).toString()
        );
        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[7.1,8.2,9.0]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains(9))]"
                ).toString()
        );
        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[10]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains(10))]"
                ).toString()
        );
        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[11]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains(11))]"
                ).toString()
        );
        assertEquals("[{\"transaction\":{\"message\":{\"accountKeys\":[12.0]}}}]",
                JSONPath.eval(
                        object,
                        "$.transactions[?(@.transaction.message.accountKeys.contains(12))]"
                ).toString()
        );
    }
}
