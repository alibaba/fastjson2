package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
class Issue7865 {
    public static class TestBean {
        public long id;
        public int count;
        public double ratio;
        public boolean enabled;
        public String name;
        public BigDecimal amount;
        public List<String> tags;
        public Map<String, Object> attributes;
    }

    public static class AbBean {
        public int ab;
    }

    @Test
    public void reportedReproducer() {
        TestBean bean = JSON.parseObject("{\"ab\":1}", TestBean.class);
        assertEquals(0, bean.id);
    }

    @Test
    public void shortFieldNameTypedBinding() {
        AbBean bean = JSON.parseObject("{\"ab\":1}", AbBean.class);
        assertEquals(1, bean.ab);
    }

    /**
     * The field-name hash fast path packed up to 8 bytes with a single {@code Unsafe}
     * read. When the array was smaller than {@code offset + 8}, the read ran past the
     * end of the buffer (only observable under a memory sanitizer such as Jazzer
     * {@code UnsafeSanitizer}), see gh-7865. This exercises names whose byte span ends
     * before the last 8 bytes of the buffer and pins the resulting hash.
     */
    @Test
    public void shortNameHashNearBufferEnd() {
        for (int len = 1; len <= 8; len++) {
            String name = repeat('a', len);
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            long expected = Fnv.hashCode64(name);
            for (int offset = 0; offset <= 2; offset++) {
                byte[] bytes = new byte[offset + len];
                System.arraycopy(nameBytes, 0, bytes, offset, len);
                long actual = Fnv.hashCode64(bytes, offset, len, true);
                assertEquals(expected, actual, "len=" + len + ", offset=" + offset);

                // same name with enough room after it takes the Unsafe fast path and
                // must produce the identical value
                byte[] padded = new byte[offset + 8];
                System.arraycopy(nameBytes, 0, padded, offset, len);
                assertEquals(expected, Fnv.hashCode64(padded, offset, len, true),
                        "padded len=" + len + ", offset=" + offset);
            }
        }
    }

    @Test
    public void boundaryFallbackMatchesFastPath() {
        for (int len = 1; len <= 8; len++) {
            byte[] raw = new byte[len];
            for (int i = 0; i < len; i++) {
                raw[i] = (byte) (0xE0 + i);
            }

            byte[] exact = new byte[len];
            System.arraycopy(raw, 0, exact, 0, len);

            byte[] padded = new byte[len + 8];
            System.arraycopy(raw, 0, padded, 0, len);

            assertEquals(
                    Fnv.hashCode64(padded, 0, len, true),
                    Fnv.hashCode64(exact, 0, len, true),
                    "len=" + len);
        }
    }

    @Test
    public void shortFieldNameAtEndOfJsonBuffer() {
        for (int len = 1; len <= 8; len++) {
            String name = repeat('a', len);
            String json = "{\"" + name + "\":" + len + "}";
            assertEquals(len, JSON.parseObject(json, Map.class).get(name));
        }
    }

    private static String repeat(char ch, int count) {
        StringBuilder buf = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            buf.append(ch);
        }
        return buf.toString();
    }
}
