# fastjson3 性能优化技巧与踩坑记录

> 基准测试：1,597 字节 minified JSON，1 个 Client（20 字段）+ 10 个 Partner（3 字段）

## 优化成果

### 读取（ClientsParseUTF8Bytes）

| 版本 | fastjson3_reflect | fastjson3_asm | wast |
|------|------------------|--------------|------|
| 优化前 | 3,068K ops/s | 2,992K ops/s | 5,039K ops/s |
| 优化后 | 5,118K ops/s | 5,128K ops/s | 5,094K ops/s |

### 写入（ClientsWriteUTF8Bytes）

| 版本 | fastjson3 | fastjson3_reflect | wast |
|------|-----------|------------------|------|
| 优化前 | 5,296K ops/s | 5,119K ops/s | 6,181K ops/s |
| 优化后 | 6,142K ops/s | 5,980K ops/s | 6,088K ops/s |

---

## 一、生效的优化

### 1. readLongOff 溢出检测：避免 BigDecimal 慢路径（贡献 ~56%）

**问题**：`readLongOff` 中 `off - start >= 19` 对所有 19 位数字无条件走 `readNumber()`（BigDecimal 解析）。
Long.MAX_VALUE = 9223372036854775807 恰好 19 位。基准数据的 long 值如 `5917278237879270907` 是合法 19 位 long，
每次都被误判为溢出。perfasm 显示此方法占 **19.51% CPU 周期**。

**修复**：对 19 位数字内联检测实际溢出（负数回绕检测），仅在真正溢出时走 BigDecimal：

```java
// 修复前
if (off - start >= 19) {
    reader.setLongValue(bean, readNumber().longValue()); // 总是走这里！
}

// 修复后
if (off - start >= 19) {
    if (off - start > 19                                    // 20+ 位一定溢出
            || (!neg && value < 0)                           // 正数回绕为负 → 溢出
            || (neg && value < 0 && value != Long.MIN_VALUE)) { // 负数溢出
        reader.setLongValue(bean, readNumber().longValue());
        return this.offset;
    }
}
reader.setLongValue(bean, neg ? -value : value); // 19 位合法 long 走快速路径
```

**教训**：
- 溢出边界检测要精确，不能用粗糙的位数判断代替实际溢出检测
- `value * 10 + digit` 在 19 位时可能溢出，但对合法 long 不会
- 溢出时 long 会回绕为负数（正数输入）或回绕为非 Long.MIN_VALUE（负数输入），可以用这个性质做内联检测
- **perfasm 是定位性能瓶颈的终极武器**，直接看到哪行汇编最热

### 2. readFieldsLoop 内联 INT / DOUBLE / BOOLEAN（贡献 ~2%）

**问题**：`readAndSetFieldUTF8Inline` 方法体 350 字节码，超过 JIT FreqInlineSize 阈值（~325），
不会被内联到 `readFieldsLoop`。每次调用需要：
- `utf8.setOffset(off)` — 写堆内存
- 虚拟方法调用开销
- `off = utf8.getOffset()` — 读堆内存

Client 有 2 个 int + 3 个 double + 1 个 boolean = 6 个字段走这个慢路径。

**修复**：在 `readFieldsLoop` 中直接调用 offset-passing 方法（readIntOff、readDoubleOff、readBooleanOff），
避免堆同步和方法调用开销。

**教训**：
- JIT 内联阈值（FreqInlineSize ~325 字节码）是硬边界，超过就不内联
- 用 `javap -c` 检查关键方法的字节码大小
- offset 作为局部变量（寄存器）vs 作为 `this.offset`（堆内存）差距显著

---

## 二、写入路径优化

### 5. writeDouble 避免 Double.toString()（贡献 ~6%）

**问题**：`writeNameDouble` 调用 `Double.toString(value)` 创建 String 对象，然后提取内部 byte[] 并复制到输出缓冲区。
每次调用产生 2 次对象分配（String + byte[]），Client 有 3 个 double 字段 = 每次解析 6 次分配。

**修复**：移植 fastjson2 的 `NumberUtils.writeDouble`（基于差分估计法的 double-to-decimal 转换），直接写入 byte[] 缓冲区。
依赖 `Scientific`、`ED`、`ED5` 查找表实现高精度转换，无需创建中间 String。

```java
// 修复前
String s = Double.toString(value);
byte[] sv = (byte[]) JDKUtils.getStringValue(s);
System.arraycopy(sv, 0, buf, count, sv.length);

// 修复后
count = NumberUtils.writeDouble(buf, count, value, true, false);
```

**参考**：这也是 wast 使用的实现方式。

### 6. PACKED_DIGITS 打包数字写入（贡献 ~3%）

**问题**：`writeLongToBytes` 每次写 2 位数字需要 2 次 byte 写入（DIGIT_TENS + DIGIT_ONES）。

**修复**：参考 OpenJDK PR [#14699](https://github.com/openjdk/jdk/pull/14699)，用 short[100] 查找表 `PACKED_DIGITS`
打包两位数字为一个 short，通过 `Unsafe.putShort` 一次写入。

```java
// 修复前
buf[--p] = DIGIT_ONES[r];  // 1 byte write
buf[--p] = DIGIT_TENS[r];  // 1 byte write

// 修复后
p -= 2;
JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[r]);  // 1 short write = 2 bytes
```

### 7. noEscape4 尾部字节优化（贡献 ~2%）

**问题**：`writeLatinStringNoCapCheck` 中 SWAR 循环后剩余 1-7 字节，逐字节做 3 次比较
（`b >= 0x20 && b != '"' && b != '\\'`），60 个字符串 × 3 尾部字节 × 3 比较 = ~540 分支。

**修复**：剩余 >= 4 字节时用 `noEscape4(int)` 批量检查+复制，减少分支数。

### 8. writeLongToBytes 拆分大数字为两个 int（贡献 ~3%）

**问题**：> INT_MAX 的 long 值（19 位数字）使用 long 除法提取数字，long 除法比 int 除法慢。

**修复**：将 long 拆为高位 int + 低 9 位 int，分别用更快的 int 除法：

```java
int hi = (int) (val / 1000000000L);
int lo = (int) (val - (long) hi * 1000000000L);
// hi 和 lo 都用 int 除法处理
```

---

## 三、尝试但未生效的优化

### 3. Long-word 头部匹配（无提升，已回退热路径）

**尝试**：用 `Unsafe.getLong` 一次比较 8 字节替代逐字节比较字段头。预计算 `hdrWord0/hdrMask0`。

**结果**：无提升。JIT 编译器已经很好地优化了逐字节比较循环（loop unrolling + 寄存器分配）。

**教训**：
- JIT 优化能力常常超出预期，手动"优化"可能反而增加指令（mask 计算、条件分支）
- 先用 perfnorm/perfasm 确认瓶颈，再优化，不要凭直觉

### 4. ASM 代码生成路径（多次尝试，均回退）

**尝试一：方法调用式有序推测（tryMatchFieldHeader）**
- ASM 生成的 readObjectUTF8 使用 `tryMatchFieldHeader` 方法调用
- 结果：fastjson3_asm 3,062K，与反射路径持平，无优势
- 原因：方法调用访问 `this.offset`（堆），而反射路径用局部变量 `off`（寄存器）

**尝试二：Offset-passing ASM（tryMatchFieldHeaderOff + readFieldSepOff）**
- ASM 生成的 readObjectUTF8 传递 `int off` 参数，返回新 offset
- 结果：回退到 2,776K，更慢
- 原因：生成的方法体过大，JIT 优化质量下降

**尝试三：ObjectMapper 默认 ASM**
- 让所有类型（Client、Partner）都用 ASM reader
- 结果：严重回退（reflect 2,661K, asm 2,776K）
- 原因：内部类型（Client、Partner）的 ASM reader 比反射 reader 慢

**教训**：
- ASM 代码生成 ≠ 更快。JIT 编译器对反射路径的小方法做了很好的优化（devirtualization、inlining、register allocation）
- ASM 生成的大方法（>325 字节码）反而阻碍 JIT 优化
- `this.offset`（堆访问）vs 局部变量 `off`（寄存器）是性能的分水岭
- 反射路径的 `readFieldsLoop` 设计（offset 作为局部变量 + 有序推测 + 内联 String/Long 读取）已经非常接近最优

---

## 三、性能分析方法论

### perfnorm：宏观指标对比

```bash
java -jar benchmark3.jar "ClientsParseUTF8Bytes.(fastjson3_reflect|wast)" \
    -wi 2 -i 3 -f 1 -t 1 -prof perfnorm
```

关键指标：
- `instructions`：总指令数（优化前 45,091 vs wast 22,361 → 2x 差距）
- `branches`：分支数（8,693 vs 4,087 → 2.1x 差距）
- `L1-dcache-loads`：内存加载次数（14,821 vs 8,613 → 1.7x 差距）
- `branch-misses`：分支预测失败（5.0 vs 1.1）

### perfasm：微观热点定位

```bash
java -jar benchmark3.jar ClientsParseUTF8Bytes.fastjson3_reflect \
    -wi 2 -i 3 -f 1 -t 1 -prof "perfasm:events=cycles;tooBigThreshold=1500"
```

这是发现 `readLongOff` 占 19.51% 周期的关键工具。直接看到哪行汇编代码最热。

### JIT 内联分析

```bash
javap -c -p 'target/classes/com/.../ReflectionObjectReader.class'
```

检查关键方法字节码大小，确认是否超过 JIT 内联阈值。

### wast 竞品分析

```bash
javap -c -p 'io/github/wycst/wast/json/JSONPojoOptimizeDeserializer.class'
```

反编译竞品了解其策略：hash 匹配 + String.indexOf（SIMD 扫描）+ 虚拟派发。

---

## 四、关键设计决策

### readFieldsLoop 的设计原则

1. **offset 作为局部变量**：`int off` 在整个循环中保持在 CPU 寄存器，仅在调用非内联方法时同步到 `this.offset`
2. **有序推测**：假设字段按声明顺序到达，逐字节比较预编码头部 `fieldNameHeader`（`"fieldName":`）
3. **方法体大小控制**：readFieldsLoop ~519 字节码，不被调用者内联，但自身作为热方法被 C2 编译器充分优化
4. **分层内联**：STRING 和 LONG 直接内联（最常见），INT/DOUBLE/BOOLEAN 通过 offset-passing 方法内联，复杂类型（LIST/POJO/ARRAY）走 readAndSetFieldUTF8Inline
5. **不可删除的"死"方法**：`readAndSetFieldUTF8`、`readListUTF8` 虽未被直接调用，但删除会改变类结构导致 JIT 编译决策变化，实测性能下降

### FieldReader 的设计

- `fieldNameHeader`：预编码 `"fieldName":` 字节数组，用于有序推测的逐字节比较
- `typeTag`：预计算类型标签（TAG_STRING=1, TAG_INT=2, ...），避免运行时 `fieldClass == String.class` 判断
- `fieldOffset`：Unsafe 字段偏移量，直接写入目标对象字段，跳过反射
- `hdrWord0/hdrMask0`：预计算长字匹配值（当前未在热路径使用，long-word 匹配实测无提升）

---

## 五、踩坑清单

| 坑 | 现象 | 原因 | 教训 |
|----|------|------|------|
| readLongOff >= 19 | 19 位合法 long 走 BigDecimal | 溢出边界判断过于保守 | 用实际溢出检测替代位数判断 |
| ASM 大方法 | ASM reader 比反射慢 | 生成方法体过大，JIT 优化质量下降 | 方法体控制在 325 字节码以内 |
| ObjectMapper 默认 ASM | 所有类型走 ASM 导致全面回退 | 内部类型的 ASM reader 不如反射 reader | 不要假设代码生成一定更快 |
| this.offset vs 局部变量 | 堆访问比寄存器慢 5-10 倍 | CPU 缓存层级差异 | 热循环中 offset 必须是局部变量 |
| 删除"无用"方法 | 性能从 718K 降至 651K | JIT 编译决策依赖类结构 | 用注释标记，不可删除 |
| long-word 头部匹配 | 无提升 | JIT 已优化逐字节循环 | 先 profile 再优化 |
| readAndSetFieldUTF8Inline 不被内联 | 350 字节码超过阈值 | FreqInlineSize ~325 | 关键方法要控制大小 |

---

## 七、参考资源

### OpenJDK PR

- **Integer/Long.toString 优化**：[openjdk/jdk#14699](https://github.com/openjdk/jdk/pull/14699)
  - 核心技巧：PACKED_DIGITS short[100] 查表，一次 putShort 写 2 位数字
  - Int.toString 提升 ~20-34%，Long.toString 提升 ~15-24%

- **数组边界检查消除**：[openjdk/jdk#23335](https://github.com/openjdk/jdk/pull/23335)
  - 核心技巧：通过代码模式帮助 JIT 消除数组边界检查

### fastjson2 实现参考

- **NumberUtils.writeDouble**：直接 double-to-bytes 转换，避免 Double.toString() 的 String 分配
  - 基于差分估计法的 IEEE 754 → 十进制转换（类似 Schubfach/Ryu 算法）
  - 依赖 ED/ED5 预计算查找表（~343 × 2 组）
  - wast 也使用相同的实现策略
