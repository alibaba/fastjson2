package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderJSONBTest {
    @Test
    public void getString() {
        String[] strings = new String[] {
                "abc",
                "abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890",
                "中文", "发展特色富民产业，扎扎实实把乡村振兴战略实施好"
        };
        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16LE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16BE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            jsonReader.readValueHashCode();
            assertEquals(string, jsonReader.getString());
        }
    }

    @Test
    public void readFieldName() {
        String[] strings = new String[]{
                "abc",
                "abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890abcdefg1234567890",
                "中文", "发展特色富民产业，扎扎实实把乡村振兴战略实施好"
        };
        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, jsonReader.readFieldName());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, jsonReader.readFieldName());
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16LE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, new String(jsonReader.readFieldName().toCharArray()));
        }

        for (String string : strings) {
            byte[] bytes = JSONB.toBytes(string, StandardCharsets.UTF_16BE);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes);
            assertEquals(string, new String(jsonReader.readFieldName().toCharArray()));
        }

        {
            SymbolTable symbolTable = JSONB.symbolTable("id");
            JSONWriter jsonWriter = JSONWriter.ofJSONB(symbolTable);
            jsonWriter.writeSymbol("id");
            byte[] bytes = jsonWriter.getBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length, symbolTable);
            assertEquals("id", jsonReader.readFieldName());
        }
    }

    @Test
    public void readFieldNameHashCodeUnquote() {
        byte[] bytes = JSONB.toBytes("id");
        JSONReader jsonReader = JSONReader.ofJSONB(bytes);
        assertEquals(Fnv.hashCode64("id"), jsonReader.readFieldNameHashCodeUnquote());
        assertFalse(jsonReader.nextIfSet());
    }

    @Test
    public void notSupported() {
        JSONReaderJSONB jsonReader = (JSONReaderJSONB) JSONReader.ofJSONB(JSONB.toBytes(""));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatch('A'));
        assertThrows(JSONException.class, () -> jsonReader.readNullOrNewDate());
        assertThrows(JSONException.class, () -> jsonReader.readNumber0());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime16());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime17());
        assertThrows(JSONException.class, () -> jsonReader.readLocalTime10());
        assertThrows(JSONException.class, () -> jsonReader.readLocalTime11());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDate11());
        assertThrows(JSONException.class, () -> jsonReader.readLocalDateTime18());
        assertThrows(JSONException.class, () -> jsonReader.readZonedDateTimeX(10));
        assertThrows(JSONException.class, () -> jsonReader.skipLineComment());
        assertThrows(JSONException.class, () -> jsonReader.readPattern());
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2'));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2', '3'));
        assertThrows(JSONException.class, () -> jsonReader.nextIfMatchIdent('0', '1', '2', '3', '4', '5'));
    }
}
