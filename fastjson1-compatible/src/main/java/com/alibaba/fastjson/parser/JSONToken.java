package com.alibaba.fastjson.parser;

public class JSONToken {
    public static final int ERROR = 1;
    public static final int LITERAL_INT = 2;
    public static final int LITERAL_FLOAT = 3;
    public static final int LITERAL_STRING = 4;
    public static final int LITERAL_ISO8601_DATE = 5;
    public static final int TRUE = 6;
    public static final int FALSE = 7;
    public static final int NULL = 8;
    public static final int NEW = 9;
    public static final int LPAREN = 10; // ("("),
    public static final int RPAREN = 11; // (")"),
    public static final int LBRACE = 12; // ("{"),
    public static final int RBRACE = 13; // ("}"),
    public static final int LBRACKET = 14; // ("["),
    public static final int RBRACKET = 15; // ("]"),
    public static final int COMMA = 16; // (","),
    public static final int COLON = 17; // (":"),
    public static final int IDENTIFIER = 18;
    public static final int FIELD_NAME = 19;
    public static final int EOF = 20;
    public static final int SET = 21;
    public static final int TREE_SET = 22;
    public static final int UNDEFINED = 23; // undefined
    public static final int SEMI = 24;
    public static final int DOT = 25;
    public static final int HEX = 26;
}
