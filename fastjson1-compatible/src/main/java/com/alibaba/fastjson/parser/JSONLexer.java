package com.alibaba.fastjson.parser;

import java.math.BigDecimal;

public interface JSONLexer {
    char EOI = 0x1A;
    int NOT_MATCH = -1;
    int NOT_MATCH_NAME = -2;
    int UNKNOWN = 0;
    int OBJECT = 1;
    int ARRAY = 2;
    int VALUE = 3;
    int END = 4;
    int VALUE_NULL = 5;

    com.alibaba.fastjson2.JSONReader getReader();

    String stringVal();

    BigDecimal decimalValue();

    boolean isBlankInput();

    int intValue();

    long longValue();

    void nextToken();

    void nextToken(int expect);

    char getCurrent();

    boolean isEOF();
}
