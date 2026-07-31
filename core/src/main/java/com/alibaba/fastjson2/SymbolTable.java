package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Symbol table for fast name lookup.
 *
 * <p>This class provides a way to efficiently map names (strings) to ordinals and vice versa.
 * It uses FNV-1a hash algorithm for fast hashing and maintains sorted hash codes for binary search.
 *
 * <p>SymbolTable is designed to be immutable after construction, making it thread-safe.
 *
 * @since 2.0.58
 */
public final class SymbolTable {
    private final String[] names;
    private final long hashCode64;
    private final short[] mapping;

    private final long[] hashCodes;
    private final long[] hashCodesOrigin;

    /**
     * Create a symbol table from class names.
     *
     * @param input classes whose names will be added to the symbol table
     * @since 2.0.58
     */
    public SymbolTable(Class<?>... input) {
        this(classNames(input));
    }

    /**
     * Extract class names from Class objects.
     *
     * @param input Class objects
     * @return array of class names
     */
    private static String[] classNames(Class<?>... input) {
        String[] names = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            names[i] = input[i].getName();
        }
        return names;
    }

    /**
     * Create a symbol table from string names.
     *
     * <p>The names will be sorted and deduplicated. Each name is assigned a unique ordinal
     * starting from 1. The ordinal 0 is reserved and means "not found".
     *
     * @param input names to be added to the symbol table
     */
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

    /**
     * Get the number of names in this symbol table.
     *
     * @return the number of names
     */
    public int size() {
        return names.length;
    }

    /**
     * Get the 64-bit hash code of this symbol table.
     *
     * <p>The hash code is computed from all the names in the symbol table.
     * It can be used to quickly compare if two symbol tables have the same content.
     *
     * @return the 64-bit hash code of this symbol table
     */
    public long hashCode64() {
        return hashCode64;
    }

    /**
     * Get the name by its hash code.
     *
     * @param hashCode the FNV-1a 64-bit hash code of the name
     * @return the name if found, {@code null} otherwise
     */
    public String getNameByHashCode(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mapping[m];
        return names[index];
    }

    /**
     * Get the ordinal of a name by its hash code.
     *
     * @param hashCode the FNV-1a 64-bit hash code of the name
     * @return the ordinal (1-based) if found, -1 otherwise
     */
    public int getOrdinalByHashCode(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return -1;
        }

        return this.mapping[m] + 1;
    }

    /**
     * Get the ordinal of a name.
     *
     * @param name the name to look up
     * @return the ordinal (1-based) if found, -1 otherwise
     */
    public int getOrdinal(String name) {
        int m = Arrays.binarySearch(hashCodes, Fnv.hashCode64(name));
        if (m < 0) {
            return -1;
        }

        return this.mapping[m] + 1;
    }

    /**
     * Get the name by its ordinal.
     *
     * @param ordinal the ordinal (1-based) of the name
     * @return the name at the specified ordinal
     * @throws ArrayIndexOutOfBoundsException if the ordinal is invalid
     */
    public String getName(int ordinal) {
        return names[ordinal - 1];
    }

    /**
     * Get the hash code of a name by its ordinal.
     *
     * @param ordinal the ordinal (1-based) of the name
     * @return the FNV-1a 64-bit hash code of the name at the specified ordinal
     * @throws ArrayIndexOutOfBoundsException if the ordinal is invalid
     */
    public long getHashCode(int ordinal) {
        return hashCodesOrigin[ordinal - 1];
    }
}
