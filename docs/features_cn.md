# 通过Features配置序列化和反序列化的行为

# 1. Feature介绍
在fastjson 2.x中，有两个Feature，分别用来配置序列化和反序列化的行为。
* JSONWriter.Feature 配置序列化的行为
* JSONReader.Feature 配置反序列化的行为


# 2. 在JSON的toJSONString和parse方法中使用Feature

## 2.1 在JSON的toJSONString方法中使用JSONWriter.Feature

```java
Bean bean = ...;
String json = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls); // 输出对象中值为null的字段
```

## 2.2 在JSON的parse方法中使用JSONReader.Feature
```java
String jsonStr = ...;
Bean bean = JSON.parseObject(jsonStr, Bean.class, JSONReader.Feature.UseBigDecimalForDoubles); // 将小数数值读取为BigDecimal
```

# 3. 在JSONField和JSONType上配置features

```java
class Model {
    @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
    public long value;
}
```

也可以在类级别使用JSONType注解：

```java
@JSONType(serializeFeatures = JSONWriter.Feature.WriteMapNullValue)
public class Model {
    public String name;
    public int age;
}
```

# 4. JSONReader.Feature介绍

| JSONReader.Feature              | 介绍                                                                                                  |
|---------------------------------|-----------------------------------------------------------------------------------------------------|
| FieldBased                      | 基于字段反序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。在fieldbase配置下会更安全 |
| IgnoreNoneSerializable          | 反序列化忽略非Serializable类型的字段                                                                            |
| SupportArrayToBean              | 支持数组映射到Bean的方式                                                                                         |
| InitStringFieldAsEmpty          | 初始化String字段为空字符串\"\"                                                                                  |
| SupportAutoType                 | 支持自动类型，要读取带\"@type\"类型信息的JSON数据，需要显式打开SupportAutoType                                                 |
| SupportSmartMatch               | 默认下是camel case精确匹配，打开这个后，能够智能识别camel/upper/pascal/snake/Kebab五种case                                  |
| UseNativeObject                 | 默认是使用JSONObject和JSONArray，配置后会使用LinkedHashMap和ArrayList                                             |
| SupportClassForName             | 支持类型为Class的字段，使用Class.forName。为了安全这个是默认关闭的                                                          |
| IgnoreSetNullValue              | 忽略输入为null的字段                                                                                        |
| UseDefaultConstructorAsPossible | 尽可能使用缺省构造函数，在fieldBase打开这个选项没打开的时候，会可能用Unsafe.allocateInstance来实现                                   |
| UseBigDecimalForFloats          | 默认配置会使用BigDecimal来parse小数，打开后会使用Float                                                               |
| UseBigDecimalForDoubles         | 默认配置会使用BigDecimal来parse小数，打开后会使用Double                                                              |
| ErrorOnEnumNotMatch             | 默认Enum的name不匹配时会忽略，打开后不匹配会抛异常                                                                       |
| TrimString                      | 对读取到的字符串值做trim处理                                                                                    |
| ErrorOnNotSupportAutoType       | 遇到AutoType报错（缺省是忽略）                                                                                 |
| DuplicateKeyValueAsArray        | 重复Key的Value不是替换而是组合成数组                                                                              |
| AllowUnQuotedFieldNames         | 支持不带双引号的字段名                                                                                         |
| NonStringKeyAsString            | 非String类型的Key当做String处理                                                                             |
| Base64StringAsByteArray         | 将Base64格式的字符串反序列化为byte[]                                                                           |
| DisableSingleQuote              | 不允许在key和value中使用单引号                                                                                   |

# 5. JSONWriter.Feature介绍

| JSONWriter.Feature                | 介绍                                                                                                               |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------|
| FieldBased                        | 基于字段序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做序列化。                                 |
| IgnoreNoneSerializable            | 序列化忽略非Serializable类型的字段                                                                                          |
| BeanToArray                       | 将对象序列为[101,\"XX\"]这样的数组格式，这样的格式会更小                                                                                 |
| WriteNulls                        | 序列化输出空值字段                                                                                                        |
| BrowserCompatible                 | 在大范围超过JavaScript支持的整数，输出为字符串格式                                                                                   |
| NullAsDefaultValue                | 将null值输出为缺省值，整数类型的Number输出为0，小数类型的Number输出为0.0，String类型输出为\"\"，Character类型输出为\\u0000，数组和Collection类型输出为[]，其余类型输出{}。 |
| WriteBooleanAsNumber              | 将true输出为1，false输出为0                                                                                              |
| WriteNonStringValueAsString       | 将非String类型的值输出为String，不包括对象和数据类型                                                                                 |
| WriteClassName                    | 序列化时输出类型信息                                                                                                       |
| NotWriteRootClassName             | 打开WriteClassName的同时，不输出根对象的类型信息                                                                                  |
| NotWriteHashMapArrayListClassName | 打开WriteClassName的同时，不输出类型为HashMap/ArrayList类型对象的类型信息，反序列结合UseNativeObject使用，能节省序列化结果的大小                          |
| NotWriteDefaultValue              | 当字段的值为缺省值时，不输出，这个能节省序列化后结果的大小                                                                                    |
| WriteEnumsUsingName               | 序列化enum使用name                                                                                                    |
| WriteEnumUsingToString            | 序列化enum使用toString方法                                                                                              |
| IgnoreErrorGetter                 | 忽略getter方法的错误                                                                                                    |
| PrettyFormat                      | 格式化输出                                                                                                            |
| PrettyFormatInlineArrays          | 配合PrettyFormat使用，将数组元素保持在同一行，而不是每个元素单独一行。与原fastjson行为一致                                                           |
| ReferenceDetection                | 打开引用检测，这个缺省是关闭的，和fastjson 1.x不一致                                                                                 |
| WriteNameAsSymbol                 | 将字段名按照symbol输出，这个仅在JSONB下起作用                                                                                     |
| WriteBigDecimalAsPlain            | 序列化BigDecimal使用toPlainString，避免科学计数法                                                                             |
| UseSingleQuotes                   | 使用单引号                                                                                                            |
| MapSortField                      | 对Map中的KeyValue按照Key做排序后再输出。在有些验签的场景需要使用这个Feature                                                                 |
| WriteNullListAsEmpty              | 将List类型字段的空值序列化输出为空数组\"[]\"                                                                                        |
| WriteNullStringAsEmpty            | 将String类型字段的空值序列化输出为空字符串\"\"                                                                                       |
| WriteNullNumberAsZero             | 将Number类型字段的空值序列化输出为0                                                                                            |
| WriteNullBooleanAsFalse           | 将Boolean类型字段的空值序列化输出为false                                                                                       |
| NotWriteEmptyArray                | 数组类型字段当length为0时不输出                                                                                              |
| WriteNonStringKeyAsString         | 将Map中的非String类型的Key当做String类型输出                                                                                  |
| ErrorOnNoneSerializable           | 序列化非Serializable对象时报错                                                                                            |
| WritePairAsJavaBean               | 将 Apache Commons 包中的Pair对象当做JavaBean序列化                                                                           |
| BrowserSecure                     | 浏览器安全，将会'<' '>' '(' ')'字符做转义输出                                                                                   |
| WriteLongAsString                 | 将Long序列化为String                                                                                                  |
| WriteEnumUsingOrdinal             | 序列化Enum使用Ordinal，缺省是name                                                                                         |
| WriteThrowableClassName           | 序列化Throwable时带上类型信息                                                                                              |
| LargeObject                       | 这个是一个保护措施，是为了防止序列化有循环引用对象消耗过大资源的保护措施。                                                                            |
| UnquoteFieldName                  | 不带引号输出Key                                                                                                        |
| NotWriteSetClassName              | 当打开WriteClassName时又不想输出Set的类型信息，使用这个Feature                                                                      |
| NotWriteNumberClassName           | 当打开WriteClassName时又不想输出Number的类型信息，比如L/S/B/F/D这种后缀，使用这个Feature                                                   |
| WriteFloatSpecialAsString         | 启用后，NaN/Infinity将被序列化为“NaN”、“Infinity”、“-Infinity”    |

# 6. 使用示例

## 6.1 序列化示例

```java
// 基本使用
User user = new User(\"张三\", 25, null);
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);

// 多个Feature组合使用
String json2 = JSON.toJSONString(user, 
    JSONWriter.Feature.WriteNulls, 
    JSONWriter.Feature.PrettyFormat);

// 使用BeanToArray特性
String json3 = JSON.toJSONString(user, JSONWriter.Feature.BeanToArray);
```

## 6.2 反序列化示例

```java
// 基本使用
String json = \"{\\\"name\\\":\\\"张三\\\",\\\"age\\\":25}\";
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportSmartMatch);

// 多个Feature组合使用
User user2 = JSON.parseObject(json, User.class, 
    JSONReader.Feature.SupportSmartMatch, 
    JSONReader.Feature.InitStringFieldAsEmpty);
```

# 7. 1.x 特性变更指南

## 7.1 默认开启的特性

在 fastjson 1.x 中，默认开启的特性如下：

**序列化**：
* `SerializerFeature.QuoteFieldNames`
* `SerializerFeature.SkipTransientField`
* `SerializerFeature.WriteEnumUsingName`
* `SerializerFeature.SortField`

**反序列化**：
* `Feature.AutoCloseSource`
* `Feature.InternFieldNames`
* `Feature.UseBigDecimal`
* `Feature.AllowUnQuotedFieldNames`
* `Feature.AllowSingleQuotes`
* `Feature.AllowArbitraryCommas`
* `Feature.SortFeidFastMatch`
* `Feature.IgnoreNotMatch`

在 fastjson 2.x 中，**所有特性默认关闭**。

## 7.2 1.x特性变更

**序列化**：
* `QuoteFieldNames`：2.x默认支持，无需配置。且2.x支持`UnquoteFieldName`特性
* `UseISO8601DateFormat`：2.x中替代方案如下：
    - `JSON.configWriterDateFormat("iso8601")`（全局配置）
    - `JSON.toJSONString(bean, "iso8601")`
    - `@JSONField(format = "iso8601")`
* `SkipTransientField`；2.x默认支持，若要关闭，方案如下：
    - JVM参数配置 `-Dfastjson2.writer.skipTransient=false`（全局配置）
    - `JSONFactory.setDefaultSkipTransient(false)`（全局配置）
    - `@JSONType(skipTransient = false)`
    - `@JSONField(skipTransient = false)`
* `SortField`：2.x默认支持，无需配置
* `WriteDateUseDateFormat`：2.x中替代方案如下：
    - `JSON.toJSONString(bean, "millis")`
    - 使用2.0.58中新增加的特性`JSONWriter.Feature.WriterUtilDateAsMillis`
* `DisableCircularReferenceDetect`：1.x中默认有循环引用检测，2.x则默认关闭。2.x若要开启，使用特性`ReferenceDetection`
* `WriteEnumUsingName`：2.x中默认关闭
* `WriteSlashAsSpecial`：2.x未支持
* `WriteTabAsSpecial`、`DisableCheckSpecialChar`在1.x中已弃用
* 其余1.x序列化特性无变化

**反序列化**：
* `AllowArbitraryCommas`：2.x的语法更加严格，不支持多重逗号
* `AllowComment`：2.x默认支持，无需配置
* `AllowISO8601DateFormat`：2.x默认支持，也可通过以下方式显式指定：
    - `JSON.configReaderDateFormat("iso8601")`（全局配置）
    - `JSON.parseObject(str, Bean.class, "iso8601")`
* `AllowSingleQuotes`：2.x默认支持，无需配置
* `AutoCloseSource`（反序列化不完整JSON抛出异常）：2.x默认支持，无需配置
* `CustomMapDeserializer`：2.x未支持
* `DisableCircularReferenceDetect`：更名为`DisableReferenceDetect`
* `DisableFieldSmartMatch`：替换为`SupportSmartMatch`（1.x中智能匹配默认开启，2.x中智能匹配默认关闭）
* `DisableSpecialKeyDetect`：2.x默认支持，无需配置
* `IgnoreAutoType`：2.x默认关闭AutoType功能，缺省配置下是安全的
* `IgnoreNotMatch`：2.x默认支持，无需配置
* `OrderedField`（反序列化为JSONObject、JSONArray时，保证声明顺序）：2.x默认支持，无需配置
* `SupportNonPublicField`：可替换为`FieldBased`
* `SafeMode`：2.x可通过JVM参数配置 `-Dfastjson2.parser.safeMode=true`
* `TrimStringFieldValue`：可替换为`TrimString`
* `UseBigDecimal`：替换为`UseBigDecimalForFloats`、`UseBigDecimalForDoubles`
* `UseNativeJavaObject`：更名为`UseNativeObject`
* `UseObjectArray`（JSON数组解析为Object[]而非ArrayList）：2.x未支持 
* 其余1.x反序列化特性无变化

# 8. 最佳实践建议

1. **安全性考虑**：
   - 默认情况下不要开启`SupportAutoType`，除非确实需要处理带类型信息的JSON
   - 对于不受信任的数据源，谨慎使用`FieldBased`特性

2. **性能优化**：
   - 对于大数据量的序列化，可以考虑使用`BeanToArray`特性来减小JSON体积
   - 在需要保持字段顺序的场景下，使用`MapSortField`特性

3. **兼容性处理**：
   - 在Web前端场景中，使用`BrowserCompatible`来处理大整数
   - 对于需要严格数据格式的场景，开启`ErrorOnEnumNotMatch`来及时发现枚举不匹配问题

4. **空值处理**：
   - 根据业务需求选择合适的空值处理策略，如`WriteNulls`、`WriteNullStringAsEmpty`等
