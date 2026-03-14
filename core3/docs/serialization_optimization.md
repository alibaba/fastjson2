# 序列化性能优化技术设计文档

## 概述

本文档记录 fastjson3 (core3) 序列化引擎在 UTF-8 byte[] 输出路径上的性能优化技术。
这些优化使 fastjson3 在 UsersWriteUTF8Bytes 基准测试中达到 854K ops/s，
超越 wast（804K ops/s）6.3%，超越 fastjson2（627K ops/s）36%。

基准测试环境：JMH，单线程，fork 2，warmup 3 iterations，measurement 5 iterations。
测试数据：Users（22 字段 POJO，含嵌套 Friend 列表），序列化为 UTF-8 byte[]。

## 架构设计

### sealed class + 子类分派

```
sealed JSONGenerator
├── Char   (char[] 缓冲区，用于 String 输出)
└── UTF8   (byte[] 缓冲区，用于 byte[] 输出)
```

使用 `sealed` 关键字让 JVM/JIT 知道子类集合是封闭的，
在调用点可以做 devirtualization（去虚化），
将虚方法调用优化为直接调用甚至内联。

### FieldWriter type tag switch 分派

```java
public final class FieldWriter {
    final int typeTag; // TYPE_STRING=1, TYPE_INT=2, ...

    public void writeField(JSONGenerator gen, Object bean, long features) {
        switch (typeTag) {
            case TYPE_STRING -> writeString(gen, bean, features);
            case TYPE_INT    -> writeInt(gen, bean, features);
            // ...
        }
    }
}
```

与传统的多态子类（`FieldWriterString extends FieldWriter`）相比：
- 调用点保持**单态**（monomorphic），JIT 可以内联 `writeField` 方法
- 避免 megamorphic vtable dispatch（当字段类型 >3 种时，JIT 退化为间接跳转）
- switch 在 JIT 编译后变成跳转表（jump table），开销接近零

## 优化技术详解

### 1. SWAR 转义字符检测 — noEscape8()

**问题：** 写入 JSON 字符串时，每个字节需要检查 4 个条件：
- `< 0x20`（控制字符）
- `== '"'`（引号）
- `== '\\'`（反斜杠）
- `> 0x7F`（非 ASCII，UTF-8 多字节）

逐字节检查在热循环中产生大量分支。

**解法：** 使用 SWAR（SIMD Within A Register）技巧，将 8 字节压缩为 long 操作：

```java
static boolean noEscape8(long v) {
    long hiMask = 0x8080808080808080L;
    long lo     = 0x0101010101010101L;

    // 检测反斜杠 '\\' (0x5C)
    // XOR 0xA3 使 0x5C→0xFF，加 0x01 溢出清除高位
    long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3L) + lo & hiMask;

    // 快速路径（3 个操作）：所有字节 > '"' (0x22) 且不是 '\\'
    // v + 0x5D 使 0x23→0x80，0x22→0x7F
    // 如果 notBackslash 的所有高位 = 1 且 v+0x5D 的所有高位 = 1 → 安全
    if ((notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask) {
        return true;  // 覆盖 >99% 的普通 ASCII 文本
    }

    // 慢速路径：精确检测控制字符和引号
    long ctrl  = (v - 0x2020202020202020L) & ~v & hiMask;  // byte < 0x20
    long xq    = v ^ 0x2222222222222222L;
    long quote = (xq - lo) & ~xq & hiMask;                 // byte == '"'
    return (ctrl | quote | (notBackslash ^ hiMask)) == 0;
}
```

**效果：** 单项贡献 ~10% 的性能提升。快速路径仅 3 个位运算 + 1 次比较，
覆盖绝大多数不含特殊字符的文本。

**原理：** SWAR 利用整数算术的进位/溢出特性来并行检测 8 个字节。
核心思想是：如果一个字节等于某个值 X，XOR 后变为 0，减 1 后借位会设置高位。
公式 `(v ^ X_REPEATED) - 0x01...01) & ~(v ^ X_REPEATED) & 0x80...80`
可以检测 v 中是否有字节等于 X。

### 2. 单遍检查+复制（Check-and-Copy in One Pass）

**问题：** 传统方式是先扫描找到需要转义的位置，再 arraycopy 安全段。
对短字符串（<32 字节），arraycopy 的 JNI 桩开销反而超过数据复制本身。

**解法：** 对每 8 字节同时做检查和复制：

```java
private void writeLatinStringNoCapCheck(byte[] value, int len) {
    int pos = count;
    buf[pos++] = '"';

    int i = 0;
    int limit8 = len - 7;
    for (; i < limit8; i += 8) {
        long v = JDKUtils.getLongDirect(value, i);  // Unsafe.getLong
        if (noEscape8(v)) {
            JDKUtils.putLongDirect(buf, pos, v);    // Unsafe.putLong
            pos += 8;
        } else {
            // 发现需转义 → 切换到逐字节处理
            pos = writeEscapedBytes(value, i, len, pos);
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
            return;
        }
    }
    // 尾部 1-7 字节逐字节处理
    for (; i < len; i++) { /* ... */ }

    buf[pos++] = '"';
    buf[pos++] = ',';
    count = pos;
}
```

**数据分析（user.json）：**
- 33% 字符串 ≤ 8 字节 → 1 次 getLong + noEscape8 + putLong
- 12% 字符串 8-16 字节 → 2 次循环
- 53% 字符串 16-32 字节 → 2-4 次循环
- 平均 17.8 字节，约 2-3 次循环迭代

**关键：** 代码路径必须简洁。过多的分支（如渐进 16→8→4→tail）会导致 JIT 放弃内联。

### 3. 字段名 long[] 预编码 + putLong 展开

**问题：** JSON 对象序列化中，`"fieldName":` 需要反复写入。
`System.arraycopy` 对 3-20 字节的短数据有固定桩开销。

**解法：** 在 FieldWriter 构造时，将 `"fieldName":` 预编码为 `long[]`：

```java
// 构造时预计算
static long[] encodeByteLongs(byte[] bytes) {
    int longCount = (bytes.length + 7) >>> 3;       // 向上取整
    byte[] padded = new byte[longCount << 3];        // 补零到 8 字节边界
    System.arraycopy(bytes, 0, padded, 0, bytes.length);
    long[] result = new long[longCount];
    for (int i = 0; i < longCount; i++) {
        result[i] = JDKUtils.getLongDirect(padded, i << 3);
    }
    return result;
}
```

写入时使用 switch 展开：

```java
private void writeNameLongsNoCheck(long[] longs, int bytesLen) {
    int pos = count;
    switch (longs.length) {
        case 1 -> putLongDirect(buf, pos, longs[0]);
        case 2 -> { putLongDirect(buf, pos, longs[0]);
                     putLongDirect(buf, pos + 8, longs[1]); }
        case 3 -> { putLongDirect(buf, pos, longs[0]);
                     putLongDirect(buf, pos + 8, longs[1]);
                     putLongDirect(buf, pos + 16, longs[2]); }
        default -> { for (int i = 0; i < longs.length; i++)
                         putLongDirect(buf, pos + (i << 3), longs[i]); }
    }
    count += bytesLen;  // 用实际字节数，不是 longs.length * 8
}
```

**注意：** 最后一个 long 的写入可能覆盖 padding 字节到缓冲区，
但 `count += bytesLen` 只推进实际字节数，后续写入会覆盖这些多余字节。

### 4. 融合 ensureCapacity（Fused Capacity Check）

**问题：** 每个字段序列化需要写 name 和 value，各自调用 ensureCapacity，
产生两次分支判断。

**解法：** 将 name 和 value 的最大空间需求合并为一次检查：

```java
public void writeNameInt32(long[] longs, int bytesLen, byte[] nameBytes,
                           char[] nameChars, int value) {
    // name(N bytes) + int(max 11 digits) + comma(1) = N + 12
    ensureCapacity(bytesLen + 12);
    writeNameLongsNoCheck(longs, bytesLen);

    int pos = count;
    pos += writeIntToBytes(value, buf, pos);
    buf[pos++] = ',';
    count = pos;
}
```

各类型最大空间：

| 类型 | 最大字节数 | ensureCapacity 参数 |
|------|-----------|-------------------|
| int | 11 + 1 | nameBytesLen + 12 |
| long | 20 + 1 | nameBytesLen + 21 |
| boolean | 5 + 1 | nameBytesLen + 6 |
| double | ~24 + 1 | nameBytesLen + 25 |
| String | len*6 + 3 | nameBytesLen + valLen*6 + 3 |

### 5. JSON.toJSONBytes() 热路径内联

**问题：** `JSON.toJSONBytes(obj)` → `ObjectMapper.writeValueAsBytes(obj)`
→ `getObjectWriter()` + `writer.write()` 有多层方法调用。

**解法：** 在 `JSON.toJSONBytes()` 中直接展开关键路径：

```java
public static byte[] toJSONBytes(Object obj) {
    if (obj == null) return NULL_BYTES;
    ObjectMapper mapper = ObjectMapper.shared();
    try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
        ObjectWriter<Object> writer = mapper.getObjectWriter(obj.getClass());
        if (writer != null) {
            writer.write(generator, obj, null, null, 0);
        } else {
            generator.writeAny(obj);
        }
        return generator.toByteArray();
    }
}
```

减少一层方法调用，帮助 JIT 决定内联边界。

### 6. Unsafe 直接内存操作

通过 `sun.misc.Unsafe` 绕过数组边界检查，直接读写内存：

```java
// JDKUtils 中的封装
static long getLongDirect(byte[] array, int offset) {
    return UNSAFE.getLong(array, BYTE_ARRAY_BASE + offset);
}

static void putLongDirect(byte[] array, int offset, long value) {
    UNSAFE.putLong(array, BYTE_ARRAY_BASE + offset, value);
}
```

用途：
- 字符串 8 字节批量读取和写入
- 字段名 long[] 批量写入
- 字段值直接读取（`getInt/getLong/getDouble` 用于 primitive 字段）

**安全保证：** ensureCapacity 总是分配 SAFE_MARGIN（512 字节）的额外空间，
确保 putLong 写入不会越界。

### 7. 数字写入优化

使用预计算查找表将两位数字一次写出：

```java
static final byte[] DIGIT_TENS = new byte[100]; // '0','0','0',...,'9','9'
static final byte[] DIGIT_ONES = new byte[100]; // '0','1','2',...,'8','9'

// 写入时从末尾开始，每次写 2 位
while (val >= 100) {
    int q = val / 100;
    int r = val - q * 100;
    val = q;
    buf[--p] = DIGIT_ONES[r];
    buf[--p] = DIGIT_TENS[r];
}
```

### 8. 缓冲区池化

`BufferPool` 使用分条带（striped）线程本地池，减少分配和 GC 压力：

```
BufferPool
├── borrowByteBuffer()   → 从线程本地池获取 byte[]
├── returnByteBuffer()   → 归还到线程本地池
├── borrowCharBuffer()   → 从线程本地池获取 char[]
└── returnCharBuffer()   → 归还到线程本地池
```

## 踩坑记录

### putLong 替代 arraycopy 写字符串值 → 回退 -18%

`System.arraycopy` 是 JVM 内建 intrinsic，对 >32 字节数据使用 AVX/SSE 指令，
比手动 putLong 循环快得多。putLong 仅在短数据（<16 字节）时有优势。

### 渐进宽度检查（16→8→4→tail）→ 回退 -5%

过多的代码路径导致 JIT 放弃内联（JIT 内联阈值约 325 字节码）。
简洁的单循环 + 尾部处理反而更快。

### 转义回退从头重写 → 数据损坏

发现需转义时，fallback 从 `i=0` 重新开始，覆盖了已通过 putLong 写入的数据。
正确做法是从当前位置 `i` 继续逐字节处理。

## 性能对比总结

| 优化项 | 贡献 | 技术 |
|--------|------|------|
| noEscape8 快速路径 | ~10% | SWAR 位运算 |
| 单遍 check-and-copy | ~8% | getLong + putLong per 8B |
| 字段名 long[] 预编码 | ~5% | Unsafe.putLong + switch 展开 |
| 融合 ensureCapacity | ~3% | 合并容量检查 |
| toJSONBytes 内联 | ~2% | 消除间接调用 |
| **总计** | **~36%** vs fastjson2 | |
