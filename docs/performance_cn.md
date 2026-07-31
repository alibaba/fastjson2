# 性能优化指南

本指南介绍 FASTJSON 2 的调优策略和最佳实践。

## 性能架构

FASTJSON 2 通过以下关键优化实现高性能：

### ASM 代码生成

FASTJSON 2 在运行时使用 ASM 为对象读取器和写入器生成优化的字节码，消除了字段访问和方法调用的反射开销。生成的代码使用基于字段名称哈希的 switch-case 语句，在反序列化时实现 O(1) 的字段查找。

- **时机**: 首次序列化/反序列化某类型时（一次性开销）
- **实现**: `ObjectReaderCreatorASM`、`ObjectWriterCreatorASM`
- **降级**: 当 ASM 不可用时使用反射创建器（如 GraalVM Native Image）

### Lambda Metafactory

在 JDK 8+ 上，FASTJSON 2 使用 `LambdaMetafactory` 创建高性能方法句柄，作为反射的替代方案。这为 getter/setter 调用提供接近原生调用的性能。

### 字符串驻留

`SymbolTable` 为频繁使用的字段名称提供高效的字符串驻留，减少解析过程中的内存分配和 GC 压力。

### 编码特化解析器

FASTJSON 2 为不同编码提供专用的解析器实现：
- `JSONReaderUTF8` - 针对 UTF-8 字节流优化
- `JSONReaderUTF16` - 针对 UTF-16（Java String 内部表示）优化
- `JSONReaderASCII` - 纯 ASCII 内容的快速路径

库会根据输入类型自动检测最优解析器。

## 调优策略

### 1. 优先使用 byte[] 而非 String

**影响: 高**

尽量直接使用 `byte[]` 而非 `String`：

```java
// 更快：从 bytes 解析
byte[] bytes = getJsonBytes(); // 来自网络、文件等
User user = JSON.parseObject(bytes, User.class);

// 更快：序列化为 bytes
byte[] output = JSON.toJSONBytes(user);
```

这避免了 String 编码/解码的开销，在 HTTP/RPC 场景中尤其有效。

### 2. 使用 BeanToArray 紧凑序列化

**影响: 中**

`BeanToArray` Feature 将对象序列化为 JSON 数组而非对象，去除字段名开销：

```java
// 输出: [1,"John",25] 而非 {"id":1,"name":"John","age":25}
String json = JSON.toJSONString(user, JSONWriter.Feature.BeanToArray);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 3. 最小化 Feature 使用

**影响: 低-中**

每个启用的 Feature 在热路径中增加一个条件检查。仅启用实际需要的 Feature：

```java
// 好：仅启用需要的
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);

// 避免：启用很多"以防万一"的 Feature
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat,        // 不需要就跳过
    JSONWriter.Feature.ReferenceDetection,  // 没有循环引用就跳过
    JSONWriter.Feature.MapSortField);       // 不关心顺序就跳过
```

### 4. 使用 FieldBased 获取最大速度

**影响: 中**

`FieldBased` 模式直接访问字段而非通过 getter/setter 方法，速度略快：

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.FieldBased);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.FieldBased);
```

> 注意：这通过反射/ASM 访问私有字段，可能在所有环境中都不可用。

### 5. 缓存自定义类型的 ObjectReader/ObjectWriter

**影响: 中**

如果注册了自定义 reader/writer，确保它们是单例：

```java
// 好：单例模式
class MoneyWriter implements ObjectWriter<Money> {
    static final MoneyWriter INSTANCE = new MoneyWriter();
    // ...
}
JSONFactory.getDefaultObjectWriterProvider().register(Money.class, MoneyWriter.INSTANCE);
```

## 线程安全

了解线程安全有助于避免不必要的同步：

| 组件 | 线程安全？ | 说明 |
|------|:---:|------|
| `JSON` 静态方法 | 是 | 主入口，始终安全 |
| `JSONObject` / `JSONArray` | 否 | 未同步，类似 `HashMap`/`ArrayList` |
| `JSONReader` / `JSONWriter` | 否 | 每次操作创建，不要跨线程共享 |
| `ObjectReader` / `ObjectWriter` | 是 | 初始化后可安全共享 |
| `ObjectReaderProvider` / `ObjectWriterProvider` | 是 | 内部缓存是线程安全的 |

## JVM 调优

### 推荐 JVM 参数

```
# 启用紧凑字符串（JDK 9+，默认开启）
-XX:+CompactStrings
```

### 内存注意事项

- FASTJSON 2 使用线程本地缓冲区进行序列化，减少 GC 压力但增加每线程内存占用。
- 对于多线程应用，请监控线程本地缓冲区使用情况。
