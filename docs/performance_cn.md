# 性能优化指南

FASTJSON 2 以极致性能为设计目标。本指南介绍调优策略和最佳实践，帮助您充分发挥库的性能。

## 性能架构

FASTJSON 2 通过以下关键优化实现高性能：

### ASM 代码生成

FASTJSON 2 在运行时使用 ASM 为对象读取器和写入器生成优化的字节码，消除了字段访问和方法调用的反射开销。生成的代码使用基于字段名称哈希的 switch-case 语句，在反序列化时实现 O(1) 的字段查找。

- **时机**: 首次序列化/反序列化某类型时（一次性开销）
- **实现**: `ObjectReaderCreatorASM`、`ObjectWriterCreatorASM`
- **降级**: 当 ASM 不可用时使用反射创建器（Android、GraalVM Native Image）

### Lambda Metafactory

在 JDK 8+ 上，FASTJSON 2 使用 `LambdaMetafactory` 创建高性能方法句柄，作为反射的替代方案。这为 getter/setter 调用提供接近原生调用的性能。

### Vector API（JDK 17+）

在 JDK 17+ 上，FASTJSON 2 利用 Vector API 的 SIMD（单指令多数据）指令进行 UTF-8 和 UTF-16 解析器中的批量字符处理。这极大地加速了字符串扫描和验证。

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

### 2. 内部通信使用 JSONB

**影响: 非常高**

对于服务间通信，JSONB（二进制 JSON）比文本 JSON 提供 3-9 倍的性能提升和更小的数据体积：

```java
// 序列化
byte[] bytes = JSONB.toBytes(user);

// 反序列化
User user = JSONB.parseObject(bytes, User.class);

// 配合 BeanToArray 更快
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 3. 大文档使用 JSONPath 部分解析

**影响: 高（针对大型文档）**

当只需从大型 JSON 文档中获取特定字段时，使用 JSONPath 提取而无需解析整个文档：

```java
// 缓存 JSONPath 实例（线程安全）
static final JSONPath ID_PATH = JSONPath.of("$.id");
static final JSONPath NAME_PATH = JSONPath.of("$.name");

// 提取特定字段
Object id = ID_PATH.extract(JSONReader.of(json));
Object name = NAME_PATH.extract(JSONReader.of(json));
```

### 4. 使用 BeanToArray 紧凑序列化

**影响: 中**

`BeanToArray` Feature 将对象序列化为 JSON 数组而非对象，去除字段名开销：

```java
// 输出: [1,"John",25] 而非 {"id":1,"name":"John","age":25}
String json = JSON.toJSONString(user, JSONWriter.Feature.BeanToArray);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 5. 最小化 Feature 使用

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

### 6. 使用 FieldBased 获取最大速度

**影响: 中**

`FieldBased` 模式直接访问字段而非通过 getter/setter 方法，速度略快：

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.FieldBased);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.FieldBased);
```

> 注意：这通过反射/ASM 访问私有字段，可能在所有环境中都不可用。

### 7. 缓存自定义类型的 ObjectReader/ObjectWriter

**影响: 中**

如果注册了自定义 reader/writer，确保它们是单例：

```java
// 好：单例模式
class MoneyWriter implements ObjectWriter<Money> {
    static final MoneyWriter INSTANCE = new MoneyWriter();
    // ...
}
JSON.register(Money.class, MoneyWriter.INSTANCE);
```

## 线程安全

了解线程安全有助于避免不必要的同步：

| 组件 | 线程安全？ | 说明 |
|------|:---:|------|
| `JSON` 静态方法 | 是 | 主入口，始终安全 |
| `JSONObject` / `JSONArray` | 否 | 未同步，类似 `HashMap`/`ArrayList` |
| `JSONReader` / `JSONWriter` | 否 | 每次操作创建，不要跨线程共享 |
| `ObjectReader` / `ObjectWriter` | 是 | 初始化后可安全共享 |
| `JSONPath` | 是 | 可缓存并在线程间重复使用 |
| `ObjectReaderProvider` / `ObjectWriterProvider` | 是 | 内部缓存是线程安全的 |

## JVM 调优

### 推荐 JVM 参数

```
# 启用紧凑字符串（JDK 9+，默认开启）
-XX:+CompactStrings

# JDK 17+，启用 Vector API 孵化器模块
--add-modules jdk.incubator.vector
```

### 内存注意事项

- FASTJSON 2 使用线程本地缓冲区进行序列化，减少 GC 压力但增加每线程内存占用。
- 对于多线程应用，请监控线程本地缓冲区使用情况。
- JSONB 通常比文本 JSON 使用更少的内存，因为编码更紧凑。

## 性能测试

FASTJSON 2 在 `benchmark/` 模块中包含 JMH 基准测试。运行方式：

```bash
cd benchmark
mvn clean package
java -jar target/benchmarks.jar
```

已发布的与 Jackson、Gson 等库的对比测试结果请参阅：
- [性能测试结果](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
- [JSONB vs Hessian/Kryo](JSONB/jsonb_vs_hessian_kryo_cn.md)
- [Android 性能测试](Android/android_benchmark_cn.md)
