# Configure serialization and deserialization with `Features`

# 1. Feature Introduction

In fastjson 2.x, there are two Features, which are used to configure the behavior of serialization and deserialization
respectively

* JSONWriter.Feature Configure serialization behavior
* JSONReader.Feature Configure deserialization behavior

# 2. Using Feature in JSON `toJSONString` and `parseObject` methods

## 2.1 Using `JSONWriter.Feature` in JSON `toJSONString` method

```java
Bean bean=...;
        JSON.toJSONString(bean,JSONWriter.Feature.WriteNulls); // Fields in the output object whose value is null
```

## 2.2 Using `JSONReader.Feature` in JSON `parseObject` method

```java
String jsonStr=...;
        JSON.parseObject(jsonStr,JSONReader.Feature.UseBigDecimalForDoubles); // Read decimal value as double
```

# 3. Configure Features on `JSONField` and `JSONType`

```java
class Model {
    @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
    public long value;
}
***

```

# 4. JSONReader.Feature

| JSONReader.Feature              | Description                                                                                                                                                                                                                                                              |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FieldBased                      | Field-based deserialization. If not configured, it will be serialized based on public field and getter methods by default. After configuration, it will be deserialized based on non-static fields (including private). It will be safer under FieldBased configuration. |
| IgnoreNoneSerializable          | Deserialization ignores fields of non-Serializable types.                                                                                                                                                                                                                |
| SupportArrayToBean              | Ways to support data mapping.                                                                                                                                                                                                                                            |
| InitStringFieldAsEmpty          | Initialize the String field to the empty string, e.g: "".                                                                                                                                                                                                                |
| SupportAutoType                 | Automatic type is supported. To read JSON data with "@type" type information, you need to open SupportAutoType explicitly.                                                                                                                                               |
| SupportSmartMatch               | The default is camel case exact match, after opening this, it can intelligently identify the case in camel/upper/pascal/snake/Kebab.                                                                                                                                     |
| UseNativeObject                 | The default is to use JSONObject and JSONArray, and LinkedHashMap and ArrayList will be used after configuration.                                                                                                                                                        |
| SupportClassForName             | To support fields of type Class, use Class.forName. This is disabled by default for security.                                                                                                                                                                            |
| IgnoreSetNullValue              | Fields with null input are ignored.                                                                                                                                                                                                                                      |
| UseDefaultConstructorAsPossible | Use the default constructor as much as possible, and use Unsafe.allocateInstance to implement this option when fieldBase is turned on and this option is not turned on.                                                                                                  | 
| UseBigDecimalForFloats          | The default configuration will use BigDecimal to parse decimals, and will use Float when turned on.                                                                                                                                                                      |
| UseBigDecimalForDoubles         | The default configuration will use BigDecimal to parse decimals, and Double will be used when turned on.                                                                                                                                                                 |
| ErrorOnEnumNotMatch             | By default, if the name of the Enum does not match, it will be ignored, and an exception will be thrown if it does not match when turned on.                                                                                                                             |
| TrimString                      |                                                                                                                                                                                                                                                                          |
| ErrorOnNotSupportAutoType       |                                                                                                                                                                                                                                                                          |
| DuplicateKeyValueAsArray        |                                                                                                                                                                                                                                                                          |
| AllowUnQuotedFieldNames         |                                                                                                                                                                                                                                                                          |
| NonStringKeyAsString            |                                                                                                                                                                                                                                                                          |
| Base64StringAsByteArray         |                                                                                                                                                                                                                                                                          |
# 5. JSONWriter.Feature

| JSONWriter.Feature                | Description                                                                                                                                                                                                             |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FieldBased                        | Field-based deserialization. If not configured, it will be serialized based on public field and getter methods by default. After configuration, it will be deserialized based on non-static fields (including private). |
| IgnoreNoneSerializable            | Serialization ignores fields of non-Serializable types.                                                                                                                                                                 |
| BeanToArray                       | Sequence the objects into an array format like [101,"XX"], which will be smaller.                                                                                                                                       |
| WriteNulls                        | Serialize write null field                                                                                                                                                                                              |
| BrowserCompatible                 | Over a wide range of integers than JavaScript supports, the output is in string format.                                                                                                                                 |
| NullAsDefaultValue                | The null value is output as the default value, the null of the Number type is output as 0, the null output of the String type is "", and the output of the array and Collection type is [].                             |
| WriteBooleanAsNumber              | Write true as 1 and false as 0.                                                                                                                                                                                         |
| WriteNonStringValueAsString       | Write values of non-String types as Strings, excluding objects and data types.                                                                                                                                          |
| WriteClassName                    | Write type information when serializing.                                                                                                                                                                                |
| NotWriteRootClassName             | When WriteClassName is turned on, the type information of the root object is not output.                                                                                                                                |
| NotWriteHashMapArrayListClassName | When WriteClassName is opened, the type information of objects of type HashMap/ArrayList is not output, and the deserialization combined with UseNativeObject can save the size of the serialized result.               |
| NotWriteDefaultValue              | When the value of the field is the default value, it is not output, which can save the size of the serialized result.                                                                                                   |
| WriteEnumsUsingName               | Serialize enum using name.                                                                                                                                                                                              |
| WriteEnumUsingToString            | Serialize enum using toString method.                                                                                                                                                                                   |
| IgnoreErrorGetter                 | Error ignoring Getter methods                                                                                                                                                                                           |
| PrettyFormat                      | formatted json string.                                                                                                                                                                                                  |
| ReferenceDetection                | Turn on reference detection, which is turned off by default, which is inconsistent with fastjson 1.x.                                                                                                                   |
| WriteNameAsSymbol                 | Output field names as symbols, this only works under JSONB.                                                                                                                                                             |
| WriteBigDecimalAsPlain            | Serialize BigDecimal using toPlainString, avoiding scientific notation.                                                                                                                                                 |
| UseSingleQuotes                   |                                                                                                                                                                                                                         |
| MapSortField                      |                                                                                                                                                                                                                         |
| WriteNullListAsEmpty              |                                                                                                                                                                                                                         |
| WriteNullStringAsEmpty            |                                                                                                                                                                                                                         |
| WriteNullNumberAsZero             |                                                                                                                                                                                                                         |
| WriteNullBooleanAsFalse           |                                                                                                                                                                                                                         |
| NotWriteEmptyArray                |                                                                                                                                                                                                                         |
| WriteNonStringKeyAsString         |                                                                                                                                                                                                                         |
| ErrorOnNoneSerializable           |                                                                                                                                                                                                                         |
| WritePairAsJavaBean               |                                                                                                                                                                                                                         |
| LargeObject                       | This is a protection measure to prevent serialization of circular reference objects from consuming excessive resources.                                                                                                 |
