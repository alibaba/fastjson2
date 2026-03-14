# fastjson3 (core3) 设计文档

## 1. 项目定位

fastjson3 是 fastjson2 的下一个大版本，目标：
- Java 17+ baseline（sealed class、pattern matching、switch expression）
- 性能超越 wast、fastjson2
- API 对标 Jackson 3（不可变 ObjectMapper、Builder 模式）
- 支持 GraalVM native-image 和 Android 8+

## 2. 核心架构

```
                          ObjectMapper (immutable, thread-safe)
                         ┌──────┴──────┐
                    ObjectReader    ObjectWriter
                    (interface)     (interface)
                         │               │
              ┌──────────┼────────┐     ┌┴─────────────┐
           Reflection   ASM    Module  Reflection   ASM
           Creator    Creator         Creator     Creator
                         │                          │
                   @JVMOnly                   @JVMOnly
```

### 解析器（sealed class 层次）

```
sealed JSONParser
├── JSONParser.Str         (String 输入)
├── JSONParser.UTF8        (byte[] 输入，主热路径)
└── JSONParser.CharArray   (char[] 输入)

sealed JSONGenerator
├── JSONGenerator.Char     (char[] 缓冲区，String 输出)
└── JSONGenerator.UTF8     (byte[] 缓冲区，byte[] 输出，主热路径)
```

`sealed` 让 JIT 做 devirtualization，将虚方法调用优化为直接调用或内联。

### Creator SPI

ObjectReader/ObjectWriter 的创建策略是可插拔的：

```java
ObjectMapper mapper = ObjectMapper.builder()
    .readerCreator(ObjectReaderCreatorASM::createObjectReader)
    .writerCreator(ObjectWriterCreatorASM::createObjectWriter)
    .build();
```

默认使用反射 Creator，通过 Builder 可切换为 ASM Creator。

### FieldNameMatcher（反序列化字段匹配）

两级匹配策略：
1. **Byte comparison**（主路径）：预编码字段名为 `byte[]`，first-byte dispatch + 直接字节比较
2. **Hash-based**（回退）：对 String/char[] 输入或遇到转义序列时使用

三种哈希策略自动选择：PLHV（加法）→ BIHV（位移）→ PRHV（质数乘法），确保零碰撞。

## 3. 性能优化

详见 [serialization_optimization.md](serialization_optimization.md)。

关键技术：
- SWAR 转义字符检测（noEscape8，8 字节并行检测）
- 单遍 check-and-copy（getLong + noEscape8 + putLong per 8B）
- 字段名 `long[]` 预编码 + putLong 展开写入
- 融合 ensureCapacity（name + value 一次容量检查）
- FieldWriter type tag switch 分派（单态调用点，避免 megamorphic dispatch）
- Unsafe 直接内存操作（绕过数组边界检查）

### ASM 字节码生成

运行时生成 ObjectReader/ObjectWriter 实现类：
- **ObjectWriterCreatorASM**：生成 `OW_BeanName_N` 类，直接 `getfield` + 调用 JSONGenerator 方法
- **ObjectReaderCreatorASM**：生成 `OR_BeanName_N` 类，`readObjectUTF8` 内联 lookupswitch 字段分派 + Unsafe `putXxx` 直接写字段

benchmark 结果（ad-hoc，非 JMH）：
- Read：ASM 比反射快 ~7%
- Write：ASM 与反射持平（反射 Writer 已通过 Unsafe 批量操作高度优化）

## 4. GraalVM native-image 支持

### 原理

`JDKUtils.NATIVE_IMAGE`：`static final boolean`，类加载时通过
`System.getProperty("org.graalvm.nativeimage.imagecode")` 检测一次。
JIT 将其常量折叠，正常 JVM 零开销。

### 分级降级

| 组件 | 正常 JVM | native-image |
|------|----------|-------------|
| ASM 字节码生成 | 正常使用 | 跳过，走反射 Creator |
| Unsafe 字段读写 | 正常使用 | 正常使用（GraalVM 支持） |
| Unsafe 数组操作 | 正常使用 | 正常使用 |
| String 内部字段操作 | 正常使用 | fallback 到 `new String()` |

### 用户侧配置

部署到 native-image 时，需要为 POJO 类提供 `reflect-config.json`
（可通过 GraalVM tracing agent 自动生成）。

## 5. Android 支持

### 兼容性

- Android 8+（API 26），AGP 8.1+ 支持 Java 17 语法（D8 脱糖）
- 所有 Java 17 语法特性（sealed、switch expression、pattern matching）均可通过 D8 脱糖向下兼容到 minSdk 21

### `@JVMOnly` 注解

标注仅 JVM 环境使用的类（ASM 字节码生成相关，共 16 个类）。
Android 构建自动排除这些类。

```java
@Retention(SOURCE)
@Target(TYPE)
@Documented
public @interface JVMOnly {}
```

### 构建方式

```bash
mvn package                    # → fastjson3-3.0.0.jar          (172KB, 62 classes)
mvn package -Pandroid          # → fastjson3-3.0.0-android.jar  (111KB, 45 classes)
```

Android profile 在 `prepare-package` 阶段扫描 `@JVMOnly` 源文件，
删除对应 `.class` 文件和 `module-info.class`。

### 裁剪内容

| 裁剪类 | 原因 |
|--------|------|
| `internal/asm/*`（13 个类） | JVM 字节码操作库，Dalvik/ART 不可用 |
| `ObjectReaderCreatorASM` | 运行时字节码生成 |
| `ObjectWriterCreatorASM` | 运行时字节码生成 |
| `DynamicClassLoader` | `ClassLoader.defineClass()`，Android 不可用 |

新增 JVM-only 类时只需加 `@JVMOnly` 注解，无需修改 pom.xml。

## 6. 模块结构

```
core3/src/main/java/com/alibaba/fastjson3/
├── JSON.java                      静态工具类
├── JSONParser.java                sealed 解析器 (Str, UTF8, CharArray)
├── JSONGenerator.java             sealed 生成器 (Char, UTF8)
├── ObjectMapper.java              不可变对象映射器
├── ObjectReader.java              反序列化接口
├── ObjectWriter.java              序列化接口
├── JSONObject.java / JSONArray.java
├── ReadFeature.java / WriteFeature.java
├── TypeReference.java
├── JSONException.java
├── annotation/
│   ├── JSONField.java             字段级注解
│   ├── JSONType.java              类级注解
│   ├── JSONCreator.java           构造器注解
│   ├── NamingStrategy.java        命名策略枚举
│   └── JVMOnly.java               JVM-only 标记注解
├── reader/
│   ├── FieldReader.java           字段读取器 (type tag dispatch)
│   ├── FieldNameMatcher.java      字段名匹配器 (byte + hash 双策略)
│   ├── ObjectReaderCreator.java   反射 Creator
│   └── ObjectReaderCreatorASM.java  @JVMOnly ASM Creator
├── writer/
│   ├── FieldWriter.java           字段写入器 (type tag dispatch)
│   ├── ObjectWriterCreator.java   反射 Creator
│   └── ObjectWriterCreatorASM.java  @JVMOnly ASM Creator
├── modules/
│   ├── ObjectReaderModule.java    读取器模块 SPI
│   └── ObjectWriterModule.java    写入器模块 SPI
├── util/
│   ├── JDKUtils.java              Unsafe + 平台检测
│   ├── UnsafeAllocator.java       跳过构造器创建实例
│   ├── BufferPool.java            缓冲区池化
│   └── DynamicClassLoader.java    @JVMOnly 动态类加载
└── internal/asm/                  @JVMOnly ASM 字节码操作库 (13 个类)
