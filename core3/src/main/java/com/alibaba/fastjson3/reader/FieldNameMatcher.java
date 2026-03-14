package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.util.JDKUtils;

import java.nio.charset.StandardCharsets;

/**
 * High-performance field name matcher with two matching strategies:
 * <ol>
 *   <li><b>Byte comparison</b> (primary): Pre-encodes field names as byte[] and uses
 *       first-byte dispatch + direct byte comparison for O(1) matching from byte[] input.
 *       Uses Unsafe bulk comparison for field names &gt; 8 bytes.</li>
 *   <li><b>Hash-based</b> (fallback): For String/char[] input or when byte comparison
 *       encounters escape sequences.</li>
 * </ol>
 *
 * @see ObjectReaderCreator
 */
public final class FieldNameMatcher {
    public static final int STRATEGY_PLHV = 0;   // addition
    public static final int STRATEGY_BIHV = 1;   // bit-shift
    public static final int STRATEGY_PRHV = 2;   // prime multiplication

    private static final long[] PRIMES = {5, 7, 11, 13, 17, 19, 23, 29, 31};

    public final int strategy;
    public final int bits;          // for BIHV
    public final long primeValue;   // for PRHV

    // Hash-based lookup table (chained, for generic fallback)
    public final Entry[] table;
    public final int mask;

    // Flat open-addressing table (cache-friendly, no pointer chasing)
    public final long[] flatHashes;
    public final FieldReader[] flatReaders;
    public final int flatMask;

    // Byte-comparison lookup: firstByte → array of candidates
    private final ByteCandidate[][] byteTable;

    private FieldNameMatcher(int strategy, int bits, long primeValue,
                             Entry[] table, int mask,
                             long[] flatHashes, FieldReader[] flatReaders, int flatMask,
                             ByteCandidate[][] byteTable) {
        this.strategy = strategy;
        this.bits = bits;
        this.primeValue = primeValue;
        this.table = table;
        this.mask = mask;
        this.flatHashes = flatHashes;
        this.flatReaders = flatReaders;
        this.flatMask = flatMask;
        this.byteTable = byteTable;
    }

    /**
     * Build a FieldNameMatcher from a set of field readers.
     */
    public static FieldNameMatcher build(FieldReader[] fieldReaders) {
        if (fieldReaders.length == 0) {
            return new FieldNameMatcher(STRATEGY_PLHV, 0, 0, new Entry[1], 0,
                    new long[1], new FieldReader[1], 0, new ByteCandidate[128][]);
        }

        // Collect all names (including alternates)
        int totalNames = 0;
        for (FieldReader fr : fieldReaders) {
            totalNames += 1 + fr.alternateNames.length;
        }

        String[] allNames = new String[totalNames];
        FieldReader[] allReaders = new FieldReader[totalNames];
        int idx = 0;
        for (FieldReader fr : fieldReaders) {
            allNames[idx] = fr.fieldName;
            allReaders[idx] = fr;
            idx++;
            for (String alt : fr.alternateNames) {
                allNames[idx] = alt;
                allReaders[idx] = fr;
                idx++;
            }
        }

        // Build byte comparison table
        ByteCandidate[][] byteTable = buildByteTable(allNames, allReaders);

        // Build hash table (for fallback)
        int hashStrategy = STRATEGY_PLHV;
        int hashBits = 0;
        long hashPrime = 0;

        if (!tryStrategy(allNames, STRATEGY_PLHV, 0, 0)) {
            boolean found = false;
            for (int b = 1; b <= 8 && !found; b++) {
                if (tryStrategy(allNames, STRATEGY_BIHV, b, 0)) {
                    hashStrategy = STRATEGY_BIHV;
                    hashBits = b;
                    found = true;
                }
            }
            if (!found) {
                for (long prime : PRIMES) {
                    if (tryStrategy(allNames, STRATEGY_PRHV, 0, prime)) {
                        hashStrategy = STRATEGY_PRHV;
                        hashPrime = prime;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                hashStrategy = STRATEGY_PRHV;
                hashPrime = 31;
            }
        }

        Entry[] hashTable = buildHashTable(allNames, allReaders, hashStrategy, hashBits, hashPrime);
        int capacity = hashTable.length;
        int hashMask = capacity - 1;

        // Build flat open-addressing table
        int flatCapacity = Integer.highestOneBit(totalNames * 4 - 1) << 1; // load factor ~0.25
        if (flatCapacity < 8) {
            flatCapacity = 8;
        }
        int fMask = flatCapacity - 1;
        long[] fHashes = new long[flatCapacity];
        FieldReader[] fReaders = new FieldReader[flatCapacity];
        for (int i = 0; i < totalNames; i++) {
            long h = computeHash(allNames[i], hashStrategy, hashBits, hashPrime);
            int slot = (int) (h & fMask);
            while (fReaders[slot] != null) {
                slot = (slot + 1) & fMask;
            }
            fHashes[slot] = h;
            fReaders[slot] = allReaders[i];
        }

        return new FieldNameMatcher(hashStrategy, hashBits, hashPrime, hashTable, hashMask,
                fHashes, fReaders, fMask, byteTable);
    }

    // ==================== Byte-based matching (primary for UTF-8) ====================

    /**
     * Get candidates for a given first byte. Used by JSONParser.UTF8 for single-pass matching.
     */
    public ByteCandidate[] getCandidates(int firstByte) {
        return byteTable[firstByte & 0x7F];
    }

    /**
     * Match a field name directly from a byte[] range.
     * Uses first-byte dispatch + byte comparison.
     */
    public FieldReader matchBytes(byte[] input, int start, int len) {
        if (len == 0) {
            return null;
        }
        int firstByte = input[start] & 0x7F;
        ByteCandidate[] candidates = byteTable[firstByte];
        if (candidates == null) {
            return null;
        }

        for (ByteCandidate c : candidates) {
            if (c.nameLen == len) {
                if (len == 1 || JDKUtils.arrayEquals(input, start + 1, c.nameBytes, 1, len - 1)) {
                    return c.reader;
                }
            }
        }
        return null;
    }

    // ==================== Hash-based matching (fallback) ====================

    /**
     * Look up a FieldReader by pre-computed hash.
     */
    public FieldReader match(long hash) {
        Entry entry = table[(int) (hash & mask)];
        while (entry != null) {
            if (entry.hash == hash) {
                return entry.fieldReader;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Look up a FieldReader using flat open-addressing table.
     * More cache-friendly than chained hash table — no pointer chasing.
     */
    public FieldReader matchFlat(long hash) {
        final long[] fh = this.flatHashes;
        final FieldReader[] fr = this.flatReaders;
        final int fm = this.flatMask;
        int slot = (int) (hash & fm);
        for (;;) {
            FieldReader r = fr[slot];
            if (r == null) {
                return null;
            }
            if (fh[slot] == hash) {
                return r;
            }
            slot = (slot + 1) & fm;
        }
    }

    /**
     * Look up with hash, verify by name comparison.
     */
    public FieldReader matchStrict(long hash, String name) {
        Entry entry = table[(int) (hash & mask)];
        while (entry != null) {
            if (entry.hash == hash && entry.name.equals(name)) {
                return entry.fieldReader;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Compute hash for a string using this matcher's hash strategy.
     */
    public long hash(String name) {
        long h = 0;
        for (int i = 0, len = name.length(); i < len; i++) {
            h = hashStep(h, name.charAt(i));
        }
        return h;
    }

    /**
     * One step of the hash computation.
     */
    public long hashStep(long hash, int c) {
        return switch (strategy) {
            case STRATEGY_PLHV -> hash + c;
            case STRATEGY_BIHV -> (hash << bits) + c;
            case STRATEGY_PRHV -> hash * primeValue + c;
            default -> hash * 31 + c;
        };
    }

    // ==================== Build helpers ====================

    private static ByteCandidate[][] buildByteTable(String[] names, FieldReader[] readers) {
        // Count candidates per first byte
        int[] counts = new int[128];
        for (String name : names) {
            if (!name.isEmpty()) {
                int fb = name.charAt(0) & 0x7F;
                counts[fb]++;
            }
        }

        // Allocate arrays
        ByteCandidate[][] table = new ByteCandidate[128][];
        int[] positions = new int[128];
        for (int i = 0; i < 128; i++) {
            if (counts[i] > 0) {
                table[i] = new ByteCandidate[counts[i]];
            }
        }

        // Fill candidates
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!name.isEmpty()) {
                int fb = name.charAt(0) & 0x7F;
                byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
                table[fb][positions[fb]++] = new ByteCandidate(nameBytes, nameBytes.length, readers[i]);
            }
        }

        return table;
    }

    private static boolean tryStrategy(String[] names, int strategy, int bits, long prime) {
        long[] hashes = new long[names.length];
        for (int i = 0; i < names.length; i++) {
            hashes[i] = computeHash(names[i], strategy, bits, prime);
        }
        for (int i = 0; i < hashes.length; i++) {
            for (int j = i + 1; j < hashes.length; j++) {
                if (hashes[i] == hashes[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static long computeHash(String name, int strategy, int bits, long prime) {
        long h = 0;
        for (int i = 0, len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            h = switch (strategy) {
                case STRATEGY_PLHV -> h + c;
                case STRATEGY_BIHV -> (h << bits) + c;
                case STRATEGY_PRHV -> h * prime + c;
                default -> h * 31 + c;
            };
        }
        return h;
    }

    private static Entry[] buildHashTable(
            String[] names, FieldReader[] readers, int strategy, int bits, long prime) {
        int capacity = Integer.highestOneBit(Math.max(names.length * 4, 1) - 1) << 1;
        if (capacity < 8) {
            capacity = 8;
        }
        int mask = capacity - 1;
        Entry[] table = new Entry[capacity];

        for (int i = 0; i < names.length; i++) {
            long h = computeHash(names[i], strategy, bits, prime);
            int slot = (int) (h & mask);
            table[slot] = new Entry(h, names[i], readers[i], table[slot]);
        }

        return table;
    }

    public static final class Entry {
        public final long hash;
        public final String name;
        public final FieldReader fieldReader;
        public final Entry next;

        Entry(long hash, String name, FieldReader fieldReader, Entry next) {
            this.hash = hash;
            this.name = name;
            this.fieldReader = fieldReader;
            this.next = next;
        }
    }

    public static final class ByteCandidate {
        public final byte[] nameBytes;
        public final int nameLen;
        public final FieldReader reader;

        ByteCandidate(byte[] nameBytes, int nameLen, FieldReader reader) {
            this.nameBytes = nameBytes;
            this.nameLen = nameLen;
            this.reader = reader;
        }
    }
}
