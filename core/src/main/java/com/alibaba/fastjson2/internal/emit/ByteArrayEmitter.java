package com.alibaba.fastjson2.internal.emit;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.internal.asm.MethodWriter;
import com.alibaba.fastjson2.util.JDKUtils;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.type;
import static com.alibaba.fastjson2.util.JDKUtils.*;

@SuppressWarnings("ALL")
public class ByteArrayEmitter {
    static final String TYPE_UNSAFE_UTILS = type(JDKUtils.class);

    private final MethodWriter mw;
    private final int slotBufer;
    private final int slotOffset;
    private final int slotQuote;
    private final long base;
    private final byte coder;

    public ByteArrayEmitter(MethodWriter mw, int slotBuffer, int slotOffset, int slotQuote, byte coder) {
        this.mw = mw;
        this.slotBufer = slotBuffer;
        this.slotOffset = slotOffset;
        this.slotQuote = slotQuote;
        this.coder = coder;
        this.base = coder == 0 ? ARRAY_BYTE_BASE_OFFSET : ARRAY_CHAR_BASE_OFFSET;
    }

    public boolean tryPutArray(int off, byte[] value) {
        if (value.length > 14) {
            return false;
        }

        putByteSlot(off++, slotQuote);
        switch (value.length) {
            case 0:
                break;
            case 1: {
                short name0 = value[0];
                 /*
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                 */
                putShort(base, off, name0);
                break;
            }
            case 2: {
                short name0 = UNSAFE.getShort(value, base);
                 /*
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                 */
                putShort(base, off, name0);
                break;
            }
            case 3: {
                short name0 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET);
                byte name1 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 2);
                 /*
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putByte(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putShort(base, off, name0);
                putByte(off + 2, name1);
                break;
            }
            case 4: {
                int name0 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET);
                 /*
                    UNSAFE.putInt(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putInt(base, off, name0);
                break;
            }
            case 5: {
                int name0 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET);
                byte name1 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 4);
                 /*
                    UNSAFE.putInt(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putInt(base, off, name0);
                putByte(off + 4, name1);
                break;
            }
            case 6: {
                int name0 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET);
                short name1 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET + 4);
                 /*
                    UNSAFE.putInt(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putInt(base, off, name0);
                putShort(base, off + 4, name1);
                break;
            }
            case 7: {
                int name0 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET);
                short name1 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET + 4);
                byte name2 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 6);
                 /*
                    UNSAFE.putInt(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                    UNSAFE.putByte(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name2);
                 */
                putInt(base, off, name0);
                putShort(base, off + 4, name1);
                putByte(off + 6, name2);
                break;
            }
            case 8: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                 */
                putLong(base, off, name0);
                break;
            }
            case 9: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                byte name1 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 8);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putByte(off + 8, name1);
                break;
            }
            case 10: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                short name1 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET + 8);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putShort(base, off + 8, name1);
                break;
            }
            case 11: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                short name1 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET + 8);
                byte name2 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 10);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putShort(base, off + 8, name1);
                putByte(off + 10, name2);
                break;
            }
            case 12: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                int name1 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET + 8);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putInt(base, off + 8, name1);
                break;
            }
            case 13: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                int name1 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET + 8);
                byte name2 = UNSAFE.getByte(value, ARRAY_BYTE_BASE_OFFSET + 12);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putInt(base, off + 8, name1);
                putByte(off + 12, name2);
                break;
            }
            case 14: {
                long name0 = UNSAFE.getLong(value, ARRAY_BYTE_BASE_OFFSET);
                int name1 = UNSAFE.getInt(value, ARRAY_BYTE_BASE_OFFSET + 8);
                short name2 = UNSAFE.getShort(value, ARRAY_BYTE_BASE_OFFSET + 12);
                 /*
                    UNSAFE.putLong(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET, name0);
                    UNSAFE.putShort(BUF, ARRAY_BYTE_BASE_OFFSET + OFFSET + 8, name1);
                 */
                putLong(base, off, name0);
                putInt(base, off + 8, name1);
                putShort(base, off + 12, name2);
                break;
            }
            default:
                throw new JSONException("TODO");
        }
        off += value.length;
        putByteSlot(off, slotQuote);
        putByte(off + 1, (byte) ':');
        return true;
    }

    public boolean tryPutArray(int off, char[] value) {
        if (value.length > 14) {
            return false;
        }
        putCharSlot(off++, slotQuote);
        switch (value.length) {
            case 0:
                break;
            case 1: {
                char name0 = value[0];
                putChar(off, name0);
                break;
            }
            case 2: {
                int name0 = UNSAFE.getInt(value, base);
                putInt(base, off, name0);
                break;
            }
            case 3: {
                int name0 = UNSAFE.getInt(value, base);
                char name1 = value[2];
                putInt(base, off, name0);
                putChar((off + 2), name1);
                break;
            }
            case 4: {
                long name0 = UNSAFE.getLong(value, base);
                putLong(base, off, name0);
                break;
            }
            case 5: {
                long name0 = UNSAFE.getLong(value, base);
                char name1 = value[4];
                putLong(base, off, name0);
                putChar((off + 4), name1);
                break;
            }
            case 6: {
                long name0 = UNSAFE.getLong(value, base);
                int name1 = UNSAFE.getInt(value, base + 8);
                putLong(base, off, name0);
                putInt(base, off + 4, name1);
                break;
            }
            case 7: {
                long name0 = UNSAFE.getLong(value, base);
                int name1 = UNSAFE.getInt(value, base + 8);
                char name2 = value[6];
                putLong(base, off, name0);
                putInt(base, (off + 4), name1);
                putChar((off + 6), name2);
                break;
            }
            case 8: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                break;
            }
            case 9: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                char name2 = value[8];
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putChar((off + 8), name2);
                break;
            }
            case 10: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                int name2 = UNSAFE.getInt(value, base + 16);
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putInt(base, (off + 8), name2);
                break;
            }
            case 11: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                int name2 = UNSAFE.getInt(value, base + 16);
                char name3 = value[10];
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putInt(base, (off + 8), name2);
                putChar((off + 10), name3);
                break;
            }
            case 12: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                long name2 = UNSAFE.getLong(value, base + 16);
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putLong(base, (off + 8), name2);
                break;
            }
            case 13: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                long name2 = UNSAFE.getLong(value, base + 16);
                char name3 = value[12];
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putLong(base, (off + 8), name2);
                putChar((off + 12), name3);
                break;
            }
            case 14: {
                long name0 = UNSAFE.getLong(value, base);
                long name1 = UNSAFE.getLong(value, base + 8);
                long name2 = UNSAFE.getLong(value, base + 16);
                int name3 = UNSAFE.getInt(value, base + 24);
                putLong(base, off, name0);
                putLong(base, (off + 4), name1);
                putLong(base, (off + 8), name2);
                putInt(base, (off + 12), name3);
                break;
            }
            default:
                throw new JSONException("TODO");
        }
        off += value.length;
        putCharSlot(off, slotQuote);
        putChar(off + 1, ':');
        return true;
    }

    public void putLong(long base, int off, long value) {
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.ldc(value);
        mw.invokevirtual("sun/misc/Unsafe", "putLong", "(Ljava/lang/Object;JJ)V");
    }

    public void putInt(long base, int off, int value) {
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iconst_n(value);
        mw.invokevirtual("sun/misc/Unsafe", "putInt", "(Ljava/lang/Object;JI)V");
    }

    public void putChar(int off, char value) {
//        mw.aload(slotBufer);
//        mw.iload(slotOffset);
//        mw.iconst_n(off);
//        mw.iadd();
//        mw.iconst_n(value);
//        mw.castore();
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iconst_n(value);
        mw.invokevirtual("sun/misc/Unsafe", "putChar", "(Ljava/lang/Object;JC)V");
    }

    public void putShort(long base, int off, short value) {
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iconst_n(value);
        mw.invokevirtual("sun/misc/Unsafe", "putShort", "(Ljava/lang/Object;JS)V");
    }

    public void putByte(int off, byte value) {
//        mw.aload(slotBufer);
//        mw.iload(slotOffset);
//        mw.iconst_n(off);
//        mw.iadd();
//        mw.iconst_n(value);
//        mw.bastore();
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iconst_n(value);
        mw.invokevirtual("sun/misc/Unsafe", "putByte", "(Ljava/lang/Object;JB)V");
    }

    public void putByteSlot(int off, int slotValue) {
//        mw.aload(slotBufer);
//        mw.iload(slotOffset);
//        mw.iconst_n(off);
//        mw.iadd();
//        mw.iload(slotValue);
//        mw.bastore();
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iload(slotValue);
        mw.invokevirtual("sun/misc/Unsafe", "putByte", "(Ljava/lang/Object;JB)V");
    }

    public void putCharSlot(int off, int slotValue) {
        mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(slotBufer);
        mw.ldc(base + (off << coder));
        mw.iload(slotOffset);
        if (coder != 0) {
            mw.iconst_1();
            mw.ishl();
        }
        mw.i2l();
        mw.ladd();
        mw.iload(slotValue);
        mw.invokevirtual("sun/misc/Unsafe", "putChar", "(Ljava/lang/Object;JC)V");
    }

    public void arrayCopy(int slotSrc) {
        /*
         * System.arraycopy(src, srcPos, dest, destPos, length);
         */
        mw.aload(slotSrc);
        mw.iconst_0();
        mw.aload(slotBufer);
        mw.iload(slotOffset);
        mw.aload(slotSrc);
        mw.arraylength();
        mw.invokestatic("java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V");
    }

    public void incArrayLength(int slotSrc) {
        mw.iload(slotOffset);
        mw.aload(slotSrc);
        mw.arraylength();
        mw.iadd();
        mw.istore(slotOffset);
    }
}
