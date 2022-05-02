package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class JSONFactory {
    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_SUPPORT = "fastjson2.autoTypeSupport";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    public static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    static final class Utils {
        static volatile ToIntFunction<String> CODER_FUNCTION;
        static volatile Function<String, byte[]> VALUE_FUNCTION;
        static volatile boolean CODER_FUNCTION_ERROR;
        static BiFunction<char[], Boolean, String> STRING_CREATOR_JDK8;
        static Function<byte[], String> STRING_CREATOR_JDK11;
        static BiFunction<byte[], Charset, String> STRING_CREATOR_JDK17;
        static volatile boolean STRING_CREATOR_ERROR = false;
    }

    static final class UUIDUtils {
        private static final byte[] NIBBLES;

        static {
            byte[] ns = new byte[256];
            Arrays.fill(ns, (byte) -1);
            ns['0'] = 0;
            ns['1'] = 1;
            ns['2'] = 2;
            ns['3'] = 3;
            ns['4'] = 4;
            ns['5'] = 5;
            ns['6'] = 6;
            ns['7'] = 7;
            ns['8'] = 8;
            ns['9'] = 9;
            ns['A'] = 10;
            ns['B'] = 11;
            ns['C'] = 12;
            ns['D'] = 13;
            ns['E'] = 14;
            ns['F'] = 15;
            ns['a'] = 10;
            ns['b'] = 11;
            ns['c'] = 12;
            ns['d'] = 13;
            ns['e'] = 14;
            ns['f'] = 15;
            NIBBLES = ns;
        }

        protected static long parse4Nibbles(byte[] name, int pos) {
            byte[] ns = NIBBLES;
            byte ch1 = name[pos];
            byte ch2 = name[pos + 1];
            byte ch3 = name[pos + 2];
            byte ch4 = name[pos + 3];
            return (ch1 | ch2 | ch3 | ch4) > 0xff ?
                    -1 : ns[ch1] << 12 | ns[ch2] << 8 | ns[ch3] << 4 | ns[ch4];
        }

        protected static long parse4Nibbles(char[] name, int pos) {
            byte[] ns = NIBBLES;
            char ch1 = name[pos];
            char ch2 = name[pos + 1];
            char ch3 = name[pos + 2];
            char ch4 = name[pos + 3];
            return (ch1 | ch2 | ch3 | ch4) > 0xff ?
                    -1 : ns[ch1] << 12 | ns[ch2] << 8 | ns[ch3] << 4 | ns[ch4];
        }

        protected static long parse4Nibbles(String name, int pos) {
            byte[] ns = NIBBLES;
            char ch1 = name.charAt(pos);
            char ch2 = name.charAt(pos + 1);
            char ch3 = name.charAt(pos + 2);
            char ch4 = name.charAt(pos + 3);
            return (ch1 | ch2 | ch3 | ch4) > 0xff ?
                    -1 : ns[ch1] << 12 | ns[ch2] << 8 | ns[ch3] << 4 | ns[ch4];
        }
    }

    static final BigDecimal LOW = BigDecimal.valueOf(-9007199254740991L);
    static final BigDecimal HIGH = BigDecimal.valueOf(9007199254740991L);
    static final BigInteger LOW_BIGINT = BigInteger.valueOf(-9007199254740991L);
    static final BigInteger HIGH_BIGINT = BigInteger.valueOf(9007199254740991L);
    static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };
    static final int[] DIGITS2 = new int[(int) 'f' + 1];

    static {
        for (int i = '0'; i <= '9'; ++i) {
            DIGITS2[i] = i - '0';
        }
        for (int i = 'a'; i <= 'f'; ++i) {
            DIGITS2[i] = (i - 'a') + 10;
        }
        for (int i = 'A'; i <= 'F'; ++i) {
            DIGITS2[i] = (i - 'A') + 10;
        }

        {
            String property = System.getProperty("fastjson2.creator");
            if (property != null) {
                property = property.trim();
            }

            CREATOR = property == null ? "asm" : property;
        }
    }

    static final class Cache {
        volatile char[] chars;

        volatile byte[] bytes0;
        volatile byte[] bytes1;
        volatile byte[] bytes2;
        volatile byte[] bytes3;

        volatile byte[] valueBytes;
    }

    static final Cache CACHE = new Cache();

    static final int CACHE_THREAD = 1024 * 1024;

    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, char[].class, "chars");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES0_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes0");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES1_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes1");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES2_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes2");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES3_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes3");

    static final AtomicReferenceFieldUpdater<Cache, byte[]> VALUE_BYTES_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(Cache.class, byte[].class, "valueBytes");

    static final class SymbolTableImpl implements JSONB.SymbolTable {
        private final String[] names;
        private final long[] hashCodesOrigin;
        private final long[] hashCodes;
        private final short[] mapping;

        private final long hashCode64;

        SymbolTableImpl(String... input) {
            {
                Set<String> nameSet = new TreeSet<>();
                for (String name : input) {
                    nameSet.add(name);
                }
                this.names = new String[nameSet.size()];
                int i = 0;
                for (String name : nameSet) {
                    names[i++] = name;
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
            for (int i = 0; i < hashCodes.length; ++i) {
                hashCode64 ^= hashCodes[i];
                hashCode64 *= Fnv.MAGIC_PRIME;
            }
            this.hashCode64 = hashCode64;
        }

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public long hashCode64() {
            return hashCode64;
        }

        @Override
        public String getNameByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return null;
            }

            int index = this.mapping[m];
            return names[index];
        }

        @Override
        public int getOrdinalByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public int getOrdinal(String name) {
            long hashCode = Fnv.hashCode64(name);
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public String getName(int ordinal) {
            return names[ordinal - 1];
        }

        @Override
        public long getHashCode(int ordinal) {
            return hashCodesOrigin[ordinal - 1];
        }
    }

    static final Properties DEFAULT_PROPERTIES = new Properties();

    static ObjectWriterProvider defaultObjectWriterProvider = new ObjectWriterProvider();
    static ObjectReaderProvider defaultObjectReaderProvider = new ObjectReaderProvider();

    static final ThreadLocal<ObjectReaderCreator> readerCreatorLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectReaderProvider> readerProviderLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectWriterCreator> writerCreatorLocal = new ThreadLocal<>();

    public static JSONWriter.Context createWriteContext() {
        return new JSONWriter.Context(defaultObjectWriterProvider);
    }

    public static JSONWriter.Context createWriteContext(JSONWriter.Feature... features) {
        return new JSONWriter.Context(defaultObjectWriterProvider, features);
    }

    public static JSONReader.Context createReadContext() {
        return new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
    }

    public static ObjectWriterProvider getDefaultObjectWriterProvider() {
        return defaultObjectWriterProvider;
    }

    public static ObjectReaderProvider getDefaultObjectReaderProvider() {
        ObjectReaderProvider providerLocal = readerProviderLocal.get();
        if (providerLocal != null) {
            return providerLocal;
        }

        return defaultObjectReaderProvider;
    }

    public static void setContextReaderCreator(ObjectReaderCreator creator) {
        readerCreatorLocal.set(creator);
    }

    public static void setContextObjectReaderProvider(ObjectReaderProvider creator) {
        readerProviderLocal.set(creator);
    }

    public static ObjectReaderCreator getContextReaderCreator() {
        return readerCreatorLocal.get();
    }

    public static void setContextWriterCreator(ObjectWriterCreator creator) {
        writerCreatorLocal.set(creator);
    }

    public static ObjectWriterCreator getContextWriterCreator() {
        return writerCreatorLocal.get();
    }
}
