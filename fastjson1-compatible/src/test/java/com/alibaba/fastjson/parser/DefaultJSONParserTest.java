package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultJSONParserTest {
    @Test
    public void test() {
        DefaultJSONParser parser = new DefaultJSONParser("[1,2]", ParserConfig.global);
        assertNotNull(parser.getLexer());
        assertNotNull(parser.getConfig());
        List<Long> array = parser.parseArray(Long.class);
        assertEquals(1L, array.get(0));
        assertEquals(2L, array.get(1));
    }

    @Test
    public void parseObject() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        LinkedHashMap map = parser.parseObject(LinkedHashMap.class);
        assertEquals(123, map.get("id"));
    }

    @Test
    public void parseObject1() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        LinkedHashMap map = parser.parseObject(new TypeReference<LinkedHashMap<String, Long>>() {
        }.getType());
        assertEquals(123L, map.get("id"));
        parser.close();
    }

    @Test
    public void parseObject2() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        JSONObject object = parser.parseObject();
        parser.handleResolveTasks(object);
        assertEquals(123, object.getIntValue("id"));
    }

    @Test
    public void parseObject3() {
        DefaultJSONParser parser = new DefaultJSONParser("null");
        JSONObject object = parser.parseObject();
        assertNull(object);
    }

    @Test
    public void parse() {
        DefaultJSONParser parser = new DefaultJSONParser("null");
        assertNull(parser.parse("abc"));
    }

    @Test
    public void parseArray() {
        String str = "[123]";
        JSONLexer lexer = new JSONScanner(str);
        DefaultJSONParser parser = new DefaultJSONParser(str, lexer, ParserConfig.global);
        List list = new ArrayList();
        parser.parseArray(Long.class, list);
        parser.handleResovleTask(list);
        assertEquals(1, list.size());
        assertEquals(123L, list.get(0));
    }

    @Test
    public void parseArray1() {
        String str = "[101,102]";
        JSONLexer lexer = new JSONScanner(str);
        DefaultJSONParser parser = new DefaultJSONParser(str, lexer, ParserConfig.global);
        Object[] array = parser.parseArray(new Type[]{Long.class, BigInteger.class});
        assertEquals(101L, array[0]);
        assertEquals(BigInteger.valueOf(102), array[1]);
    }

    @Test
    public void parseArray2() {
        String str = "[123]";
        JSONLexer lexer = new JSONScanner(str);
        DefaultJSONParser parser = new DefaultJSONParser(str, lexer, ParserConfig.global);
        List list = new ArrayList();
        parser.parseArray(list);
        assertEquals(1, list.size());
        assertEquals(123, list.get(0));
    }

    @Test
    public void parseArray3() {
        String str = "[123]";
        JSONLexer lexer = new JSONScanner(str);
        DefaultJSONParser parser = new DefaultJSONParser(str, lexer, ParserConfig.global);
        List list = new ArrayList();
        parser.parseArray((Type) Long.class, list);
        assertEquals(1, list.size());
        assertEquals(123L, list.get(0));
    }

    @Test
    public void accept() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        parser.accept(JSONToken.LBRACE);
        parser.accept(JSONToken.LITERAL_STRING);
        parser.accept(JSONToken.COLON);
        parser.accept(JSONToken.LITERAL_INT);
        parser.accept(JSONToken.RBRACE);
    }

    @Test
    public void accept2() {
        DefaultJSONParser parser = new DefaultJSONParser("[1,2.3,null]");
        parser.accept(JSONToken.LBRACKET);
        parser.accept(JSONToken.LITERAL_INT);
        parser.accept(JSONToken.COMMA);
        parser.accept(JSONToken.LITERAL_FLOAT);
        parser.accept(JSONToken.COMMA);
        parser.accept(JSONToken.NULL);
        parser.accept(JSONToken.RBRACKET);
    }

    @Test
    public void accept3() {
        DefaultJSONParser parser = new DefaultJSONParser("[true,false]");
        parser.accept(JSONToken.LBRACKET);
        parser.accept(JSONToken.TRUE);
        parser.accept(JSONToken.COMMA);
        parser.accept(JSONToken.FALSE);
        parser.accept(JSONToken.RBRACKET);
    }

    @Test
    public void accept4() {
        DefaultJSONParser parser = new DefaultJSONParser("Set[1,2]");
        parser.accept(JSONToken.SET);
        parser.accept(JSONToken.LBRACKET);
        parser.accept(JSONToken.LITERAL_INT);
        parser.accept(JSONToken.COMMA);
        parser.accept(JSONToken.LITERAL_INT);
        parser.accept(JSONToken.RBRACKET);
    }

    @Test
    public void accept5() {
        DefaultJSONParser parser = new DefaultJSONParser("Set[1,2]");
        int[] tokens = new int[] {
                JSONToken.LBRACKET,
                JSONToken.RBRACKET,
                JSONToken.LBRACE,
                JSONToken.RBRACE,
                JSONToken.LPAREN,
                JSONToken.RPAREN,
                JSONToken.DOT,
                JSONToken.COMMA,
                JSONToken.COLON,
                JSONToken.LITERAL_INT,
                JSONToken.LITERAL_FLOAT,
                JSONToken.LITERAL_STRING,
                JSONToken.NULL,
                JSONToken.TRUE,
                JSONToken.FALSE
        };
        for (int token : tokens) {
            assertThrows(Exception.class, () -> parser.accept(token));
        }
    }
}
