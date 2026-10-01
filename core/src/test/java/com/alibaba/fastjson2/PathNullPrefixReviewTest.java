package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PathNullPrefixReviewTest {
    @Test
    public void explicitNullAndMissingArrayPrefix() {
        JSONPath path = JSONPath.of(new String[]{"$[0][0]", "$[0][1]"}, new Type[]{Integer.class, Integer.class});
        for (String input : new String[]{"[]", "[null]"}) {
            try (JSONReader text = JSONReader.of(input);
                    JSONReader jsonb = JSONReader.ofJSONB(JSONB.toBytes(JSON.parse(input)))) {
                if ("[]".equals(input)) {
                    assertArrayEquals(new Object[2], (Object[]) path.extract(text));
                    assertArrayEquals(new Object[2], (Object[]) path.extract(jsonb));
                } else {
                    assertNull(path.extract(text));
                    assertNull(path.extract(jsonb));
                }
            }
        }
    }
}
