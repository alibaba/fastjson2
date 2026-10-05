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
 * Names use {@link Fnv#hashCode64(String)}, including its compact encoding for short names,
 * with sorted hash codes for binary search. Hash lookups do not resolve collisions by comparing names.
 * <details><summary>中文</summary>
 * 名称使用 fastjson 的 64 位哈希（包含短名称紧凑编码）并通过二分查找定位；哈希查找不比较名称以消除冲突。
 * </details>
 *
 * <p>SymbolTable is designed to be immutable after construction, making it thread-safe.
 *
 * @since 2.0.58
 */
public final class SymbolTable {
    private final String[] names;
    private final long hashCode64;
    // Keep full indexes for tables with more than 32,768 names.
    // <details><summary>中文</summary>保留完整索引，支持超过 32,768 个名称的符号表。</details>
    private final int[] mapping;

    private final long[] hashCodes;
    private final long[] hashCodesOrigin;

    /**
     * Creates a symbol table from the binary names returned by {@link Class#getName()}.
     * <details><summary>中文</summary>使用 Class.getName() 返回的二进制名称创建符号表。</details>
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
     * starting from 1 in natural string order. Ordinal lookups return -1 when no matching hash is found.
     * <details><summary>中文</summary>
     * 名称按字符串自然顺序排序并去重，从 1 开始编号；序号查找未找到匹配哈希时返回 -1。
     * </details>
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

        mapping = new int[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = i;
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
     * <p>The fingerprint is computed from the sorted, deduplicated names. Equal fingerprints
     * alone do not guarantee equal contents because hash collisions are possible.
     * <details><summary>中文</summary>
     * 根据排序去重后的名称计算指纹；哈希可能冲突，因此指纹相同不能保证内容相同。
     * </details>
     *
     * @return the 64-bit hash code of this symbol table
     */
    public long hashCode64() {
        return hashCode64;
    }

    /**
     * Gets a name using its {@link Fnv#hashCode64(String)} hash.
     * <details><summary>中文</summary>通过 fastjson 名称哈希查找名称。</details>
     *
     * @param hashCode the fastjson 64-bit hash of the name
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
     * Gets a name's one-based ordinal using its {@link Fnv#hashCode64(String)} hash.
     * <details><summary>中文</summary>通过 fastjson 名称哈希查找从 1 开始的序号。</details>
     *
     * @param hashCode the fastjson 64-bit hash of the name
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
     * Gets the {@link Fnv#hashCode64(String)} hash of a name by its one-based ordinal.
     * <details><summary>中文</summary>根据从 1 开始的序号返回 fastjson 名称哈希。</details>
     *
     * @param ordinal the ordinal (1-based) of the name
     * @return the fastjson 64-bit hash of the name at the specified ordinal
     * @throws ArrayIndexOutOfBoundsException if the ordinal is invalid
     */
    public long getHashCode(int ordinal) {
        return hashCodesOrigin[ordinal - 1];
    }
}
