package com.alibaba.fastjson3.util;

/**
 * Thread-local buffer pool for reusing char[] and byte[] arrays in JSON generators.
 * Inspired by wast's JSONCharArrayWriter/JSONByteArrayWriter buffer caching strategy.
 *
 * <p>Uses a striped pool (one slot per thread group) to minimize contention.
 * When a generator is created, it borrows a buffer from the pool. When closed, it returns
 * the buffer. If the pool slot is occupied, the buffer is simply discarded (GC-friendly).</p>
 *
 * <p>Default buffer size is 4096 (matching wast). Buffers larger than 256KB are not returned
 * to the pool to prevent memory leaks from rare large payloads.</p>
 */
public final class BufferPool {
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_RECYCLE_SIZE = 256 * 1024;

    // Striped pool based on available processors
    private static final int POOL_SIZE = Integer.highestOneBit(
            Math.max(2, Runtime.getRuntime().availableProcessors())) << 1;
    private static final int POOL_MASK = POOL_SIZE - 1;

    private static final CharSlot[] CHAR_SLOTS = new CharSlot[POOL_SIZE];
    private static final ByteSlot[] BYTE_SLOTS = new ByteSlot[POOL_SIZE];

    private static final ThreadLocal<Integer> THREAD_INDEX = ThreadLocal.withInitial(() -> {
        // Distribute threads across slots
        return (int) (Thread.currentThread().threadId() & POOL_MASK);
    });

    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            CHAR_SLOTS[i] = new CharSlot();
            BYTE_SLOTS[i] = new ByteSlot();
        }
    }

    private BufferPool() {
    }

    // ==================== Char buffer ====================

    /**
     * Borrow a char[] buffer. Returns a pooled buffer if available, else allocates a new one.
     */
    public static char[] borrowCharBuffer() {
        int idx = THREAD_INDEX.get();
        CharSlot slot = CHAR_SLOTS[idx];
        char[] buf;
        synchronized (slot) {
            buf = slot.buffer;
            if (buf != null) {
                slot.buffer = null;
                return buf;
            }
        }
        return new char[BUFFER_SIZE];
    }

    /**
     * Return a char[] buffer to the pool.
     * Buffers larger than MAX_RECYCLE_SIZE are discarded.
     */
    public static void returnCharBuffer(char[] buf) {
        if (buf == null || buf.length > MAX_RECYCLE_SIZE) {
            return;
        }
        int idx = THREAD_INDEX.get();
        CharSlot slot = CHAR_SLOTS[idx];
        synchronized (slot) {
            slot.buffer = buf;
        }
    }

    // ==================== Byte buffer ====================

    /**
     * Borrow a byte[] buffer. Returns a pooled buffer if available, else allocates a new one.
     */
    public static byte[] borrowByteBuffer() {
        int idx = THREAD_INDEX.get();
        ByteSlot slot = BYTE_SLOTS[idx];
        byte[] buf;
        synchronized (slot) {
            buf = slot.buffer;
            if (buf != null) {
                slot.buffer = null;
                return buf;
            }
        }
        return new byte[BUFFER_SIZE];
    }

    /**
     * Return a byte[] buffer to the pool.
     * Buffers larger than MAX_RECYCLE_SIZE are discarded.
     */
    public static void returnByteBuffer(byte[] buf) {
        if (buf == null || buf.length > MAX_RECYCLE_SIZE) {
            return;
        }
        int idx = THREAD_INDEX.get();
        ByteSlot slot = BYTE_SLOTS[idx];
        synchronized (slot) {
            slot.buffer = buf;
        }
    }

    private static final class CharSlot {
        char[] buffer;
    }

    private static final class ByteSlot {
        byte[] buffer;
    }
}
