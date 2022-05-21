# FASTJSON 1.x升级指南

## 1. 为什么要升级
* 性能更好，具体性能数据 https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark
* 支持JDK新特性，包括JDK 14引入的Record，Lambda表达式的更原生支持，GraalVM Native-Image支持
* 原生支持kotlin
* 支持 JSON Schema https://alibaba.github.io/fastjson2/json_schema_cn
* 统一文本和二进制API，在RPC、Redis场景也可以使用FASTJSON v2
* 更安全，完全删除autoType白名单，提升安全性 https://alibaba.github.io/fastjson2/fastjson2_autotype_cn
* 新版本会长期维护，目标为下一个时间提供高性能JSON库，提需求能更快得到响应，提BUG也更快修复

## 2. 如何升级

### 2.1. 如何获得最新版本
FASTJSON v2项目目前处于活跃状态，会不定期发布新版本，你可以在fastjson2发布地址中获得最新版本 https://github.com/alibaba/fastjson2/releases


可以两种模式升级：
* 兼容模式
* 使用fastjson v2新的API

### 2.2. 兼容模式升级
升级可以通过兼容模式升级，兼容模式不需要改代码，但在深度使用的场景，不能做到完全兼容，通过这样的模式升级虽然省事，请认证测试，遇到问题反馈到 https://github.com/alibaba/fastjson2/issues

* 兼容模式Maven依赖
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

### 2.3. 使用新API升级
使用新API是建议的升级方式，使用新的API能获得更多的功能。

* 包名编程
  `FASTJSON` v2和1.x版本使用不同的package，新的package名称是com.alibaba.fastjson2，新package和之前不同，可以实现1.x和2.x共存

```java
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
```

* Maven依赖
  Maven依赖的groupId和1.x不同，使用了新的groupId`com.alibaba.fastjson2`
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

如果你需要用到spring支持的功能，还需要依赖`fastjson2-extension`
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

如果你是在kotlin中使用fastjson，需要依赖`fastjson2-kotlin`
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

## 4. 常见问题
## 4.1. ParserConfig.getGlobalInstance().addAccept()如何兼容
在2.x版本中，ParserConfig添加autoType白名单的功能在ObjectReaderProvider中提供，可以如下的方式配置autoType白名单。
```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

## 4.2. ObjectSerializer 和 ObjectDeserializer 被移除了，有什么新的代替方案
FASTJSON v2中有比较完善的扩展机制，如下：
* Annotation介绍 https://github.com/alibaba/fastjson2/wiki/fastjson2_annotations
* Annotation注入介绍 https://github.com/alibaba/fastjson2/wiki/mixin_cn
* Feature介绍 https://github.com/alibaba/fastjson2/wiki/Features_cn
* 使用Mixin注入Anntation定制序列化和反序列化 https://github.com/alibaba/fastjson2/wiki/mixin_cn
* 实现ObjectWriter和ObjectReader实现定制序列化和反序列化 https://github.com/alibaba/fastjson2/wiki/register_custom_reader_writer_cn

