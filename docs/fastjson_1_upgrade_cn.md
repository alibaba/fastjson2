# FASTJSON 1.x升级指南

## 1. 为什么要升级
* 性能更好，整体性能优于1.x版本
* 支持JDK新特性，包括JDK 14引入的Record，Lambda表达式的更原生支持，GraalVM Native-Image支持
* 更安全，完全删除autoType白名单，AutoType缺省关闭，提升安全性

## 2. 如何升级

### 2.1. 版本说明
本仓库是基于fastjson2 2.0.63裁剪的版本，仅保留core模块，提供核心的JSON序列化与反序列化功能，不包含1.x兼容包及其它扩展模块。

升级方式是使用fastjson v2新的API。

### 2.2. 使用新API升级
使用新API是建议的升级方式，使用新的API能获得更多的功能。

* 包名变更
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

* 行为差异
  * AutoType缺省关闭，1.x默认带白名单，2.x没有任何白名单，必须显式打开
  * 所有Feature缺省关闭，1.x默认开启多个Feature
  * 循环引用检测缺省关闭，1.x默认开启
  * SmartMatch缺省关闭，1.x默认开启
  * 方法名有调整，例如`JSON.parse(String)`返回Object，1.x中常用的解析入口在2.x中对应`JSON.parseObject(String, Class)`，序列化入口仍是`JSON.toJSONString(Object)`

## 3. 常见问题
### 3.1. 1.x中autoType白名单配置如何替代
在1.x中，autoType白名单通过全局配置添加；在2.x中，对应的功能由ObjectReaderProvider提供，可以如下的方式配置autoType白名单。
```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

### 3.2. ObjectSerializer 和 ObjectDeserializer 被移除了，有什么新的代替方案
FASTJSON v2中有比较完善的扩展机制，如下：
* Annotation介绍 [annotations_cn.md](annotations_cn.md)
* Feature介绍 [features_cn.md](features_cn.md)
* 使用Mixin注入Annotation定制序列化和反序列化 [mixin_cn.md](mixin_cn.md)
* 实现ObjectWriter和ObjectReader实现定制序列化和反序列化 [register_custom_reader_writer_cn.md](register_custom_reader_writer_cn.md)

### 3.3. 常见的类扩展升级映射
| fastjson1                                                   | fastjson2                                             |
|-------------------------------------------------------------|-------------------------------------------------------|
| com.alibaba.fastjson.parser.deserializer.ExtraProcessor     | com.alibaba.fastjson2.filter.ExtraProcessor           |
| com.alibaba.fastjson.parser.deserializer.ObjectDeserializer | com.alibaba.fastjson2.reader.ObjectReader             |
| com.alibaba.fastjson.serializer.AfterFilter                 | com.alibaba.fastjson2.filter.AfterFilter              |
| com.alibaba.fastjson.serializer.BeforeFilter                | com.alibaba.fastjson2.filter.BeforeFilter             |
| com.alibaba.fastjson.serializer.ContextValueFilter          | com.alibaba.fastjson2.filter.ContextValueFilter       |
| com.alibaba.fastjson.serializer.LabelFilter                 | com.alibaba.fastjson2.filter.LabelFilter              |
| com.alibaba.fastjson.serializer.NameFilter                  | com.alibaba.fastjson2.filter.NameFilter               |
| com.alibaba.fastjson.serializer.PascalNameFilter            | com.alibaba.fastjson2.filter.PascalNameFilter         |
| com.alibaba.fastjson.serializer.PropertyFilter              | com.alibaba.fastjson2.filter.PropertyFilter           |
| com.alibaba.fastjson.serializer.ObjectSerializer            | com.alibaba.fastjson2.writer.ObjectWriter             |
| com.alibaba.fastjson.serializer.SerializeConfig             | com.alibaba.fastjson2.writer.ObjectWriterProvider     |
| com.alibaba.fastjson.serializer.ValueFilter                 | com.alibaba.fastjson2.filter.ValueFilter              |
| com.alibaba.fastjson.serializer.SerializerFeature           | com.alibaba.fastjson2.JSONWriter.Feature              |
| com.alibaba.fastjson.parser.Feature                         | com.alibaba.fastjson2.JSONReader.Feature              |


### 3.4 SerializerFeature.UseISO8601DateFormat在fastjson2的替代方案

fastjson2的JSONWriter.Feature没有和UseISO8601DateFormat的Feature，代替方法是使用format="iso8601"，如下：
```java
import com.alibaba.fastjson2.JSON;

String format = "iso8601";
JSON.toJSONString(obj, format);
```

### 3.5 SerializerFeature.DisableCircularReferenceDetect在fastjson2的替代方案
在fastjson2中，代替的是JSONWriter.Feature.ReferenceDetection，但语义相反，缺省不一样。fastjson2中的JSONWriter.Feature.ReferenceDetection缺省是关闭的，而fastjson1缺省是打开的。

### 3.6 SerializerFeature.SortField在fastjson2的替代方案
不需要，在fastjson2中，JSONObject继承自LinkedHashMap，不需要配置这个Feature

### 3.7 SerializerFeature.WriteDateUseDateFormat在fastjson2的替代方案
在fastjson2中的缺省行为就是使用dateFormat，如果要修改为成和fastjson 1.x一样的行为，需要配置format = "millis"，如下：
```java
import com.alibaba.fastjson2.JSON;

String format = "millis";
JSON.toJSONString(obj, format);
```
