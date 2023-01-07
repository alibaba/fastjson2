package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.SymbolTable;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONB.Constants.BC_SYMBOL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymbolTest {
    /**
     * 0x7f
     */
    @Test
    public void testSymbol() {
        SymbolTable symbolTable = JSONB.symbolTable("id", "name");
        Map map = new LinkedHashMap();

        String k1 = "id";
        int v1 = 1;

        String k2 = "name";
        String v2 = "DataWorks";

        map.put(k1, v1);
        map.put(k2, v2);

        byte[] bytes = JSONB.toBytes(map, symbolTable);
        assertEquals(17, bytes.length);
        assertEquals(BC_OBJECT, bytes[0]);
        assertEquals(BC_OBJECT_END, bytes[bytes.length - 1]);

        byte[] k1Bytes = JSONB.toBytes(-symbolTable.getOrdinal("id"));
        byte[] v1Bytes = JSONB.toBytes(v1);

        byte[] k2Bytes = JSONB.toBytes(-symbolTable.getOrdinal("name"));
        byte[] v2Bytes = JSONB.toBytes(v2);

        assertEquals(bytes.length, 4 + k1Bytes.length + v1Bytes.length + k2Bytes.length + v2Bytes.length);

        assertEquals(BC_SYMBOL, bytes[1]);
        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 2], k1Bytes[i]);
        }

        for (int i = 0; i < v1Bytes.length; i++) {
            assertEquals(bytes[i + 2 + k1Bytes.length], v1Bytes[i]);
        }

        assertEquals(BC_SYMBOL, bytes[2 + k1Bytes.length + v1Bytes.length]);
        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + v1Bytes.length], k2Bytes[i]);
        }

        for (int i = 0; i < v2Bytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + v1Bytes.length + k2Bytes.length], v2Bytes[i]);
        }

        Map parsed = (Map) JSONB.parse(bytes, symbolTable);
        assertEquals(2, parsed.size());
        assertEquals(v1, parsed.get(k1));
        assertEquals(v2, parsed.get(k2));
    }

    /**
     * 0x7f
     */
    @Test
    public void testSymbol1() {
        SymbolTable symbolTable = JSONB.symbolTable("id", "name");
        Bean bean = new Bean();

        String k1 = "id";
        int v1 = 1;

        String k2 = "name";
        String v2 = "DataWorks";

        bean.id = v1;
        bean.name = v2;

        byte[] bytes = JSONB.toBytes(bean, symbolTable);
        assertEquals(17, bytes.length);
        assertEquals(BC_OBJECT, bytes[0]);
        assertEquals(BC_OBJECT_END, bytes[bytes.length - 1]);

        byte[] k1Bytes = JSONB.toBytes(-symbolTable.getOrdinal("id"));
        byte[] v1Bytes = JSONB.toBytes(v1);

        byte[] k2Bytes = JSONB.toBytes(-symbolTable.getOrdinal("name"));
        byte[] v2Bytes = JSONB.toBytes(v2);

        assertEquals(bytes.length, 4 + k1Bytes.length + v1Bytes.length + k2Bytes.length + v2Bytes.length);

        assertEquals(BC_SYMBOL, bytes[1]);
        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 2], k1Bytes[i]);
        }

        for (int i = 0; i < v1Bytes.length; i++) {
            assertEquals(bytes[i + 2 + k1Bytes.length], v1Bytes[i]);
        }

        assertEquals(BC_SYMBOL, bytes[2 + k1Bytes.length + v1Bytes.length]);
        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + v1Bytes.length], k2Bytes[i]);
        }

        for (int i = 0; i < v2Bytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + v1Bytes.length + k2Bytes.length], v2Bytes[i]);
        }

        Map parsed = (Map) JSONB.parse(bytes, symbolTable);
        assertEquals(2, parsed.size());
        assertEquals(v1, parsed.get(k1));
        assertEquals(v2, parsed.get(k2));
    }

    public static class Bean {
        public int id;
        public String name;
    }
}
