package com.alibaba.fastjson2.adapter.jackson.core;

public interface JsonTokenId {
    int ID_START_OBJECT = 1;
    int ID_END_OBJECT = 2;
    int ID_START_ARRAY = 3;
    int ID_END_ARRAY = 4;
    int ID_FIELD_NAME = 5;
    int ID_STRING = 6;
    int ID_NUMBER_INT = 7;
    int ID_NUMBER_FLOAT = 8;
    int ID_TRUE = 9;
    int ID_FALSE = 10;
    int ID_NULL = 11;
}
