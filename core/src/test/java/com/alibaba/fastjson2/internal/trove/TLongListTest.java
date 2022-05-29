package com.alibaba.fastjson2.internal.trove;

import org.junit.jupiter.api.Test;

import java.util.*;

public class TLongListTest {
    @Test
    public void test_0() {
        TLongSet set = new TLongSet();
        HashMap map = new HashMap();

        set.addSymbol(1);
        set.addSymbol(3);
        set.addSymbol(2);

        set.addSymbol(4);
        set.addSymbol(5);
    }

    @Test
    public void test_1() {
        TLongSet set = new TLongSet();
        Map<Long, Integer> map = new TreeMap<>();

        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            long val = random.nextInt(1000);
            int index = set.addSymbol(val);
            Integer mapIndex = map.putIfAbsent(val, map.size());
            if (mapIndex == null) {
                int insertPoint = -(index + 1);
                if (insertPoint != map.size() - 1) {
                    throw new IllegalStateException();
                }
            } else if (index != mapIndex.intValue()) {
                throw new IllegalStateException();
            }
        }

        System.out.println();
    }

    static class TLongSet {
        long[] symbolHashCodes;
        int symbolHashCodesSize;
        int[] symbolIndexes;

        public TLongSet() {
            int capacity = 16;
            this.symbolHashCodes = new long[capacity];
            this.symbolIndexes = new int[capacity];
        }

        public int addSymbol(long hashCode) {
            int index = Arrays.binarySearch(symbolHashCodes, 0, symbolHashCodesSize, hashCode);
            if (index >= 0) {
                return symbolIndexes[index];
            }

            if (symbolHashCodesSize == symbolHashCodes.length) {
                symbolHashCodes = Arrays.copyOf(symbolHashCodes, symbolHashCodesSize + 16);
                symbolIndexes = Arrays.copyOf(symbolIndexes, symbolHashCodesSize + 16);
            }

            int insertPoint = -(index + 1);
            if (insertPoint < symbolHashCodesSize) {
                System.arraycopy(symbolHashCodes, insertPoint, symbolHashCodes, insertPoint + 1, symbolHashCodesSize - insertPoint);
                System.arraycopy(symbolIndexes, insertPoint, symbolIndexes, insertPoint + 1, symbolHashCodesSize - insertPoint);
            }
            symbolHashCodes[insertPoint] = hashCode;
            symbolIndexes[insertPoint] = symbolHashCodesSize;
            symbolHashCodesSize++;

            return -symbolHashCodesSize;
        }
    }
}
