package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author 张治保
 * @since 2024/10/22
 */
public class Issue3123 {
    @Test
    @SneakyThrows
    void test() {
        BigDecimal num1 = BigDecimal.valueOf(107.29305145106407);
        BigDecimal num2 = BigDecimal.valueOf(23.42835970945431);
        BigDecimal num3 = BigDecimal.valueOf(23.427479896686418);

        Double num11 = 107.29305145106407;
        Double num21 = 23.42835970945431;
        Double num31 = 23.427479896686418;

        Map map = new LinkedHashMap();
        map.put("b1", num1);
        map.put("b2", num2);
        map.put("b3", num3);

        map.put("d1", num11);
        map.put("d2", num21);
        map.put("d3", num31);

        assertEquals(
                "{\"b1\":107.29305145106407,\"b2\":23.42835970945431,\"b3\":23.427479896686418,\"d1\":107.29305145106407,\"d2\":23.42835970945431,\"d3\":23.427479896686418}",
                JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible));
    }
}
