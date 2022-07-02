package com.alibaba.fastjson2;

public interface SymbolTable {
    long hashCode64();

    String getNameByHashCode(long hashCode);

    int getOrdinalByHashCode(long hashCode);

    int getOrdinal(String name);

    String getName(int ordinal);

    long getHashCode(int ordinal);

    int size();
}
