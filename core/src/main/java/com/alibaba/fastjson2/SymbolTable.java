package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public final class SymbolTable {
    private final String[] names;
    private final long hashCode64;
    private final short[] mapping;

    private final long[] hashCodes;
    private final long[] hashCodesOrigin;

    public SymbolTable(String... input) {
        Set<String> set = new TreeSet<>(Arrays.asList(input));
        names = new String[set.size()];
        Iterator<String> it = set.iterator();

        for (int i = 0; i < names.length; i++) {
            if (it.hasNext()) {
                names[i] = it.next();
            }
        }

        long[] hashCodes = new long[names.length];
        for (int i = 0; i < names.length; i++) {
            long hashCode = Fnv.hashCode64(names[i]);
            hashCodes[i] = hashCode;
        }
        this.hashCodesOrigin = hashCodes;

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }

        long hashCode64 = Fnv.MAGIC_HASH_CODE;
        for (long hashCode : hashCodes) {
            hashCode64 ^= hashCode;
            hashCode64 *= Fnv.MAGIC_PRIME;
        }
        this.hashCode64 = hashCode64;
    }

    public int size() {
        return names.length;
    }

    public long hashCode64() {
        return hashCode64;
    }

    public String getNameByHashCode(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mapping[m];
        return names[index];
    }

    public int getOrdinalByHashCode(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return -1;
        }

        return this.mapping[m] + 1;
    }

    public int getOrdinal(String name) {
        int m = Arrays.binarySearch(hashCodes, Fnv.hashCode64(name));
        if (m < 0) {
            return -1;
        }

        return this.mapping[m] + 1;
    }

    public String getName(int ordinal) {
        return names[ordinal - 1];
    }

    public long getHashCode(int ordinal) {
        return hashCodesOrigin[ordinal - 1];
    }
}
