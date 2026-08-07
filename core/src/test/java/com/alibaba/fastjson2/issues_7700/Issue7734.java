package com.alibaba.fastjson2.issues_7700;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for #7734: a {@code BigDecimal} field annotated with Jackson's
 * {@code @JsonFormat(shape = STRING)} produced invalid JSON such as
 * {@code {"amount":string200}} instead of {@code {"amount":"200"}}.
 *
 * <p>Root cause: the {@code "string"} sentinel (set for shape=STRING) was fed
 * to {@code new DecimalFormat(...)} as a pattern, so {@code writeDecimal} took
 * the formatted-string branch and emitted the raw pattern text unquoted. The
 * fix keeps {@code WriteNonStringValueAsString} (set for shape=STRING) in charge
 * and does not build a {@code DecimalFormat} from the sentinel.
 */
@Tag("regression")
public class Issue7734 {
    public static class Bean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal amount;
    }

    @Test
    public void bigDecimalStringShapeSerializesAsQuotedString() {
        Bean bean = new Bean();
        bean.amount = new BigDecimal("6.56000000001");
        String json = JSON.toJSONString(bean);
        // must be valid JSON with the value quoted as a string
        assertEquals("{\"amount\":\"6.56000000001\"}", json);
        Bean parsed = JSON.parseObject(json, Bean.class);
        assertEquals(new BigDecimal("6.56000000001"), parsed.amount);
    }

    @Test
    public void bigDecimalStringShapeNullRoundTripsWithoutCorruption() {
        Bean bean = new Bean();
        // amount is null — must not emit the malformed "string" token
        String json = JSON.toJSONString(bean);
        Bean parsed = JSON.parseObject(json, Bean.class);
        assertNull(parsed.amount);
    }

    public static class PatternBean {
        // a real DecimalFormat pattern (no shape=STRING) must still be applied,
        // proving the fix only skips the "string" sentinel, not genuine patterns
        @JsonFormat(pattern = "0.00")
        public BigDecimal amount;
    }

    @Test
    public void realDecimalFormatPatternStillApplied() {
        PatternBean bean = new PatternBean();
        bean.amount = new BigDecimal("7");
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("7.00"), "expected formatted value 7.00 in: " + json);
    }
}
