package com.alibaba.fastjson2;

import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;

import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static final boolean GRAALVM = false;
    public static final boolean ANDROID = false;

    public static ObjectReaderCreator[] readerCreators() {
        return new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE,
        };
    }

    public static ObjectWriterCreator[] writerCreators() {
        return new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE,
                ObjectWriterCreatorASM.INSTANCE,
        };
    }

    public static ObjectReaderCreator[] readerCreators2() {
        return new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE,
        };
    }

    public static JSONWriter[] createJSONWriters() {
        if (JDKUtils.STRING_VALUE == null) {
            if (JDKUtils.JVM_VERSION == 8) {
                if (JDKUtils.UNSAFE_SUPPORT) {
                    return new JSONWriter[]{
                            new JSONWriterUTF8(JSONFactory.createWriteContext()),
                            new JSONWriterUTF16(JSONFactory.createWriteContext()),
                            new JSONWriterUTF16JDK8(JSONFactory.createWriteContext()),
                            new JSONWriterUTF16JDK8UF(JSONFactory.createWriteContext())
                    };
                }
                return new JSONWriter[]{
                        new JSONWriterUTF8(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16JDK8(JSONFactory.createWriteContext())
                };
            } else {
                if (JDKUtils.UNSAFE_SUPPORT) {
                    return new JSONWriter[]{
                            new JSONWriterUTF8(JSONFactory.createWriteContext()),
                            new JSONWriterUTF16(JSONFactory.createWriteContext())
                    };
                }
                return new JSONWriter[]{
                        new JSONWriterUTF8(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16(JSONFactory.createWriteContext())
                };
            }
        }

        if (JDKUtils.JVM_VERSION == 8) {
            if (JDKUtils.UNSAFE_SUPPORT) {
                return new JSONWriter[]{
                        new JSONWriterUTF8(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16JDK8(JSONFactory.createWriteContext()),
                        new JSONWriterUTF16JDK8UF(JSONFactory.createWriteContext())
                };
            }

            return new JSONWriter[]{
                    new JSONWriterUTF8(JSONFactory.createWriteContext()),
                    new JSONWriterUTF16(JSONFactory.createWriteContext()),
                    new JSONWriterUTF16JDK8(JSONFactory.createWriteContext())
            };
        }

        if (JDKUtils.UNSAFE_SUPPORT) {
            return new JSONWriter[]{
                    new JSONWriterUTF8JDK9(JSONFactory.createWriteContext()),
                    new JSONWriterUTF16(JSONFactory.createWriteContext()),
                    new JSONWriterUTF16JDK9UF(JSONFactory.createWriteContext())
            };
        }

        return new JSONWriter[]{
                new JSONWriterUTF16(JSONFactory.createWriteContext()),
                new JSONWriterUTF16JDK8(JSONFactory.createWriteContext())
        };
    }

    public static ObjectReaderCreator READER_CREATOR = ObjectReaderCreatorASM.INSTANCE;
    public static ObjectWriterCreator WRITER_CREATOR = ObjectWriterCreatorASM.INSTANCE;

    public static <T> ObjectReader<T> createObjectReaderLambda(Class<T> objectClass) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectClass);
    }

    public static <T> ObjectWriter<T> createObjectWriterLambda(Class<T> objectClass) {
        // TODO
        return ObjectWriterCreator.INSTANCE.createObjectWriter(objectClass);
    }

    public static ObjectReaderCreator readerCreator(ClassLoader classLoader) {
        return new ObjectReaderCreatorASM(classLoader);
    }

    public static ObjectWriterCreator writerCreator(ClassLoader classLoader) {
        return new ObjectWriterCreatorASM(classLoader);
    }

    public static <T> ObjectReader<T> of(Class<T> objectType) {
        return READER_CREATOR.createObjectReader(objectType);
    }

    public static final String encodeToBase64String(byte[] sArr, boolean lineSep) {
        // Reuse char[] since we can't create a String incrementally anyway and StringBuffer/Builder would be slower.
        return new String(encodeToChar(sArr, lineSep));
    }

    private static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
        char[] CA = IOUtils.CA;

        // Check special case
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new char[0];
        }

        int eLen = (sLen / 3) * 3; // Length of even 24-bits.
        int cCnt = ((sLen - 1) / 3 + 1) << 2; // Returned character count
        int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0); // Length of returned array
        char[] dArr = new char[dLen];

        // Encode even 24-bits
        for (int s = 0, d = 0, cc = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

            // Encode the int into four chars
            dArr[d++] = CA[(i >>> 18) & 0x3f];
            dArr[d++] = CA[(i >>> 12) & 0x3f];
            dArr[d++] = CA[(i >>> 6) & 0x3f];
            dArr[d++] = CA[i & 0x3f];

            // Add optional line separator
            if (lineSep && ++cc == 19 && d < dLen - 2) {
                dArr[d++] = '\r';
                dArr[d++] = '\n';
                cc = 0;
            }
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = sLen - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

            // Set last four chars
            dArr[dLen - 4] = CA[i >> 12];
            dArr[dLen - 3] = CA[(i >>> 6) & 0x3f];
            dArr[dLen - 2] = left == 2 ? CA[i & 0x3f] : '=';
            dArr[dLen - 1] = '=';
        }
        return dArr;
    }

    public static JSONReader[] createJSONReaders(String str) {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] utf16Bytes = str.getBytes(StandardCharsets.UTF_16);
        return new JSONReader[]{
                new JSONReaderUTF8(JSONFactory.createReadContext(), null, utf8Bytes, 0, utf8Bytes.length),
                new JSONReaderUTF16(JSONFactory.createReadContext(), utf16Bytes, 0, utf16Bytes.length)
        };
    }

    public static JSONReader[] createJSONReaders4(String str) {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] utf16Bytes = str.getBytes(StandardCharsets.UTF_16);
        return new JSONReader[]{
                new JSONReaderUTF8(JSONFactory.createReadContext(), null, utf8Bytes, 0, utf8Bytes.length),
                new JSONReaderUTF16(JSONFactory.createReadContext(), utf16Bytes, 0, utf16Bytes.length),
                new JSONReaderASCII(JSONFactory.createReadContext(), null, utf8Bytes, 0, utf8Bytes.length)
        };
    }

    public static JSONReader[] createJSONReaders2(String str) {
        byte[] utf16Bytes = str.getBytes(StandardCharsets.UTF_16);
        return new JSONReader[]{
                new JSONReaderUTF16(JSONFactory.createReadContext(), utf16Bytes, 0, utf16Bytes.length),
        };
    }

    public static JSONReader createJSONReaderStr(String str, JSONReader.Feature... features) {
        return JSONReader.of(str, JSONFactory.createReadContext(features));
    }
}
