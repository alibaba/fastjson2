# 通过Features配置序列化和反序列化的行为

# 1. Feature介绍
在fastjson 2.x中，有两个Feature，分别用来配置序列化和反序列化的行为。
* JSONWriter.Feature 配置序列化的行为
* JSONReader.Feature 配置反序列化的行为


# 2. 在JSON的toJSONString和parse方法中使用Feature

## 2.1 在JSON的toJSONString方法中使用JSONWriter.Feature

```java
Bean bean = ...;
JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls); // 输出对象中值为null的字段
```

## 2.2 在JSON的parse方法中使用JSONReader.Feature
```java
String jsonStr = ...;
JSON.parseObject(jsonStr, JSONReader.Feature.UseBigDecimalForDoubles); // 将小数数值读取为double
```

# 3. 在JSONField和JSONType上配置features

```java
class Model {
    @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
    public long value;
}
***

```

# 4. JSONReader.Feature介绍

| JSONReader.Feature              | 介绍                                                                                                  |
|---------------------------------|-----------------------------------------------------------------------------------------------------|
| FieldBased                      | 基于字段反序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。在fieldbase配置下会更安全 |
| IgnoreNoneSerializable          | 反序列化忽略非Serializable类型的字段                                                                            |
| SupportArrayToBean              | 支持数据映射的方式                                                                                           |
| InitStringFieldAsEmpty          | 初始化String字段为空字符串""                                                                                  |
| SupportAutoType                 | 支持自动类型，要读取带"@type"类型信息的JSON数据，需要显示打开SupportAutoType                                                 |
| SupportSmartMatch               | 默认下是camel case精确匹配，打开这个后，能够智能识别camel/upper/pascal/snake/Kebab五中case                                 |
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
| Base64StringAsByteArray         | 将byte[]序列化为Base64格式的字符串                                                                             |

# 5. JSONWriter.Feature介绍

| JSONWriter.Feature                | 介绍                                                                                      |
|-----------------------------------|-----------------------------------------------------------------------------------------|
| FieldBased                        | 基于字段反序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。      |
| IgnoreNoneSerializable            | 序列化忽略非Serializable类型的字段                                                                 |
| BeanToArray                       | 将对象序列为[101,"XX"]这样的数组格式，这样的格式会更小                                                        |
| WriteNulls                        | 序列化输出空值字段                                                                               |
| BrowserCompatible                 | 在大范围超过JavaScript支持的整数，输出为字符串格式                                                          |
| NullAsDefaultValue                | 将空置输出为缺省值，Number类型的null都输出为0，String类型的null输出为""，数组和Collection类型的输出为[]                   |
| WriteBooleanAsNumber              | 将true输出为1，false输出为0                                                                     |
| WriteNonStringValueAsString       | 将非String类型的值输出为String，不包括对象和数据类型                                                        |
| WriteClassName                    | 序列化时输出类型信息                                                                              |
| NotWriteRootClassName             | 打开WriteClassName的同时，不输出根对象的类型信息                                                         |
| NotWriteHashMapArrayListClassName | 打开WriteClassName的同时，不输出类型为HashMap/ArrayList类型对象的类型信息，反序列结合UseNativeObject使用，能节省序列化结果的大小 |
| NotWriteDefaultValue              | 当字段的值为缺省值时，不输出，这个能节省序列化后结果的大小                                                           |
| WriteEnumsUsingName               | 序列化enum使用name                                                                           |
| WriteEnumUsingToString            | 序列化enum使用toString方法                                                                     |
| IgnoreErrorGetter                 | 忽略setter方法的错误                                                                           |
| PrettyFormat                      | 格式化输出                                                                                   |
| ReferenceDetection                | 打开引用检测，这个缺省是关闭的，和fastjson 1.x不一致                                                        |
| WriteNameAsSymbol                 | 将字段名按照symbol输出，这个仅在JSONB下起作用                                                            |
| WriteBigDecimalAsPlain            | 序列化BigDecimal使用toPlainString，避免科学计数法                                                    |
| UseSingleQuotes                   | 使用单引号                                                                                   |
| MapSortField                      | 对Map中的KeyValue按照Key做排序后再输出。在有些验签的场景需要使用这个Feature                                        |
| WriteNullListAsEmpty              | 将List类型字段的空值序列化输出为空数组"[]"                                                               |
| WriteNullStringAsEmpty            | 将String类型字段的空值序列化输出为空字符串""                                                              |
| WriteNullNumberAsZero             | 将Number类型字段的空值序列化输出为0                                                                   |
| WriteNullBooleanAsFalse           | 将Boolean类型字段的空值序列化输出为false                                                              |
| NotWriteEmptyArray                | 数组类型字段当length为0时不输出                                                                     |
| WriteNonStringKeyAsString         | 将Map中的非String类型的Key当做String类型输出                                                         |
| ErrorOnNoneSerializable           | 序列化非Serializable对象时报错                                                                   |
| WritePairAsJavaBean               | 将 Apache Common 包中的Pair对象当做JavaBean序列化                                                  |
| LargeObject                       | 这个是一个保护措施，是为了防止序列化有循环引用对象消耗过大资源的保护措施。                                 |
