package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteBigDecimalAsPlain;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3595 {
    @Test
    public void test() throws Exception {
        BigDecimal dec = BigDecimal.valueOf(12345678, -30);
        try (JSONWriter writer = JSONWriter.ofUTF16(WriteBigDecimalAsPlain, PrettyFormat)) {
            writer.writeDecimal(dec);
            assertEquals(dec.toPlainString(), writer.toString());
        }
    }
}
