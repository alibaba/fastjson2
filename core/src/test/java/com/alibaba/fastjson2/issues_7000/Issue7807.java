package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue 7807: JSON.parse on a document that ends inside a number's exponent
 * throws ArrayIndexOutOfBoundsException instead of JSONException.
 *
 * Regression: readNumber0 reads the exponent sign/digits without an
 * end-of-input guard, so "1e", "1e-" and "1e+" (and their uppercase / fraction
 * variants) overflow the byte/char buffer.
 */
public class Issue7807 {
    @Test
    public void testTruncatedExponentThrowsJSONException() {
        String[] inputs = {
                "1e", "1E",
                "1e-", "1E-", "1e+", "1E+",
                "1.0e", "1.0E", "1.0e-", "1.0e+",
                "-1e", "-1E", "-1e-", "-1e+",
        };
        for (String input : inputs) {
            assertThrows(JSONException.class,
                    () -> JSON.parse(input),
                    "expected JSONException for input: " + input);
        }
    }
}
