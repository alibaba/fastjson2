package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ASM JSONB writer reserves a whole field group's capacity up front
 * ({@code ensureCapacity(offset + minCapacity)}) and then writes the fields straight into the
 * byte array with no further bounds checks, using Unsafe stores for the field names and for
 * the multi-byte numeric forms. Every capacity helper therefore has to be an upper bound on
 * what the matching write helper actually emits.
 *
 * <p>{@code JSONB.IO.stringCapacity(null)} returned 0 while {@code JSONB.IO.writeString} emits
 * one {@code BC_NULL} byte for null, so each null String field under-reserved the group by one
 * byte whenever nulls were written. Depending on which store crossed the end of the buffer the
 * result was either an ArrayIndexOutOfBoundsException or - when the crossing store was an
 * Unsafe {@code putShortUnaligned}/{@code putLongUnaligned} - a silent one-byte write into the
 * neighboring heap object. Overwriting a mark word that way surfaces later as a SIGSEGV inside
 * GC, which is how issue #7927 presented.
 *
 * <p>The capacity contract is pinned directly rather than only through a bean, because whether
 * a given bean overflows depends on unrelated over-reservation elsewhere in the same group
 * absorbing the shortfall.
 */
public class JSONBNullStringCapacityTest {
    /**
     * writeString(null) is a single BC_NULL byte, so the capacity reserved for it must not be 0.
     */
    @Test
    public void nullStringCapacityMatchesWhatIsWritten() {
        byte[] buf = new byte[16];
        int end = JSONB.IO.writeString(buf, 0, (String) null);
        assertEquals(1, end, "writeString(null) emits exactly one byte");
        assertEquals(JSONB.Constants.BC_NULL, buf[0]);
        assertTrue(JSONB.IO.stringCapacity((String) null) >= end,
                "stringCapacity(null) must cover the BC_NULL byte that writeString emits");
    }

    /**
     * The general invariant: for every value, reserved capacity >= bytes actually written.
     * Null is the case that regressed; the rest guard the surrounding length boundaries.
     */
    @Test
    public void capacityCoversWrittenBytesForAllStringShapes() {
        String[] values = {
                null, "", "J", "ab",
                rep('x', 46), rep('x', 47), rep('x', 48), rep('x', 200), rep('x', 2047), rep('x', 2048),
                "\u4e2d", "\u4e2d\u6587abc", rep('\u4e2d', 50)
        };
        byte[] buf = new byte[1 << 16];
        for (String s : values) {
            int written = JSONB.IO.writeString(buf, 0, s);
            int reserved = JSONB.IO.stringCapacity(s);
            assertTrue(reserved >= written,
                    "under-reserved for " + (s == null ? "null" : "String(len=" + s.length() + ")")
                            + ": reserved=" + reserved + " written=" + written);
        }
    }

    /**
     * Null elements of a String collection/array are summed through the same helper, so they
     * under-reserved the enclosing frame by one byte each as well. Enough nulls are used to
     * exhaust the fixed headroom these two overloads add, which would otherwise mask the
     * shortfall and let the assertion pass on the broken code.
     */
    @Test
    public void nullElementsInStringContainersAreCovered() {
        String[] withNulls = new String[18];
        Arrays.fill(withNulls, null);
        withNulls[0] = "J";
        withNulls[17] = rep('x', 47);

        byte[] buf = new byte[1 << 16];
        int written = JSONB.IO.writeString(buf, 0, withNulls, 0);
        assertTrue(JSONB.IO.stringCapacity(withNulls) >= written,
                "String[] with null elements under-reserved: reserved="
                        + JSONB.IO.stringCapacity(withNulls) + " written=" + written);

        written = JSONB.IO.writeString(buf, 0, Arrays.asList(withNulls), 0);
        assertTrue(JSONB.IO.stringCapacity(Arrays.asList(withNulls)) >= written,
                "Collection<String> with null elements under-reserved: reserved="
                        + JSONB.IO.stringCapacity(Arrays.asList(withNulls)) + " written=" + written);
    }

    /**
     * End-to-end through the generated ASM writer. Starting from a 1-byte buffer makes
     * ensureCapacity size the array to exactly the computed minimum (1.5x growth cannot cover
     * it), so a one-byte shortfall has nowhere to hide.
     *
     * <p>Five null Strings under-reserve by 5 while the int 74 over-reserves by 3 (fieldCapacity
     * reserves 5, its two-byte form writes 2), leaving the group 2 bytes short: capacity 19 for
     * 21 bytes of output. The int is emitted by a bare Unsafe putShortUnaligned at indices 18-19,
     * so before the fix its low byte 0x4A landed at index 19 - past the end - without any
     * exception, and the bounds-checked closing marker then threw at index 20. An exception
     * naming index 20 rather than 19 is what proves the store at 19 went unchecked. Asserting
     * both that the call succeeds and that off never exceeds the buffer covers that silent case.
     */
    @Test
    public void asmDirectGroupStaysInsideTheBuffer() {
        NullsAndInt bean = new NullsAndInt();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider()
                .getObjectWriter(NullsAndInt.class, NullsAndInt.class, 0);

        JSONWriterJSONB writer = new JSONWriterJSONB(
                JSONFactory.createWriteContext(JSONWriter.Feature.WriteNulls), null);
        writer.bytes = new byte[1];
        writer.off = 0;
        objectWriter.writeJSONB(writer, bean, null, null, JSONWriter.Feature.WriteNulls.mask);
        assertTrue(writer.off <= writer.bytes.length,
                "wrote past the buffer end: off=" + writer.off + " capacity=" + writer.bytes.length);
        // the group must have been sized to its computed minimum, otherwise the default buffer
        // would absorb the shortfall and this test would pass without exercising the bug
        assertTrue(writer.bytes.length < 128,
                "capacity amplifier did not engage, capacity=" + writer.bytes.length);

        // and the encoding still round-trips
        byte[] encoded = Arrays.copyOf(writer.bytes, writer.off);
        NullsAndInt decoded = JSONB.parseObject(encoded, NullsAndInt.class);
        assertEquals(74, decoded.v);
    }

    private static String rep(char c, int n) {
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    public static class NullsAndInt {
        public String a;
        public String b;
        public String c;
        public String d;
        public String e;
        public int v = 74;
    }
}
