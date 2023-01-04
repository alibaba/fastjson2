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

    public static String name(int value) {
        switch (value) {
            case ERROR:
                return "error";
            case LITERAL_INT:
                return "int";
            case LITERAL_FLOAT:
                return "float";
            case LITERAL_STRING:
                return "string";
            case LITERAL_ISO8601_DATE:
                return "iso8601";
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NULL:
                return "null";
            case NEW:
                return "new";
            case LPAREN:
                return "(";
            case RPAREN:
                return ")";
            case LBRACE:
                return "{";
            case RBRACE:
                return "}";
            case LBRACKET:
                return "[";
            case RBRACKET:
                return "]";
            case COMMA:
                return ",";
            case COLON:
                return ":";
            case SEMI:
                return ";";
            case DOT:
                return ".";
            case IDENTIFIER:
                return "ident";
            case FIELD_NAME:
                return "fieldName";
            case EOF:
                return "EOF";
            case SET:
                return "Set";
            case TREE_SET:
                return "TreeSet";
            case UNDEFINED:
                return "undefined";
            case HEX:
                return "hex";
            default:
                return "Unknown";
        }
    }
}
