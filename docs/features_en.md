# Configure serialization and deserialization with `Features`

# 1. Feature Introduction

In fastjson 2.x, there are two Features, which are used to configure the behavior of serialization and deserialization respectively.

* JSONWriter.Feature Configure serialization behavior
* JSONReader.Feature Configure deserialization behavior

# 2. Using Feature in JSON `toJSONString` and `parseObject` methods

## 2.1 Using `JSONWriter.Feature` in JSON `toJSONString` method

```java
Bean bean = ...;
String json = JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls); // Fields in the output object whose value is null
```

## 2.2 Using `JSONReader.Feature` in JSON `parseObject` method

```java
String jsonStr = ...;
Bean bean = JSON.parseObject(jsonStr, Bean.class, JSONReader.Feature.UseBigDecimalForDoubles); // Read decimal values as BigDecimal
```

# 3. Configure Features on `JSONField` and `JSONType`

```java
class Model {
    @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
    public long value;
}
```

You can also use the `@JSONType` annotation at the class level:

```java
@JSONType(serializeFeatures = JSONWriter.Feature.WriteMapNullValue)
public class Model {
    public String name;
    public int age;
}
```

# 4. JSONReader.Feature

| JSONReader.Feature              | Description                                                                                                                                                                                                                                                              |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FieldBased                      | Field-based deserialization. If not configured, it will be serialized based on public field and getter methods by default. After configuration, it will be deserialized based on non-static fields (including private). It will be safer under FieldBased configuration. |
| IgnoreNoneSerializable          | Deserialization ignores fields of non-Serializable types.                                                                                                                                                                                                                |
| SupportArrayToBean              | Support mapping arrays to Bean objects.                                                                                                                                                                                                                                  |
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
| TrimString                      | Trim the string values read.                                                                                                                                                                                                                                             |
| ErrorOnNotSupportAutoType       | Throw an error when encountering AutoType (default is to ignore).                                                                                                                                                                                                        |
| DuplicateKeyValueAsArray        | Duplicate Key Values are not replaced but combined into an array.                                                                                                                                                                                                        |
| AllowUnQuotedFieldNames         | Support field names without double quotes.                                                                                                                                                                                                                               |
| NonStringKeyAsString            | Treat non-String keys as String.                                                                                                                                                                                                                                         |
| Base64StringAsByteArray         | Deserialize Base64 formatted strings as byte[].                                                                                                                                                                                                                          |
| DisableSingleQuote              | Do not allow single quote in key name and values.                                                                                                                                                                                                                        |

# 5. JSONWriter.Feature

| JSONWriter.Feature                | Description                                                                                                                                                                                                             |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FieldBased                        | Field-based serialization. If not configured, it will be serialized based on public field and getter methods by default. After configuration, it will be serialized based on non-static fields (including private).        |
| IgnoreNoneSerializable            | Serialization ignores fields of non-Serializable types.                                                                                                                                                                 |
| BeanToArray                       | Sequence the objects into an array format like [101,"XX"], which will be smaller.                                                                                                                                       |
| WriteNulls                        | Serialize and output null value fields.                                                                                                                                                                                 |
| BrowserCompatible                 | For integers that exceed the range supported by JavaScript, output them in string format.                                                                                                                               |
| NullAsDefaultValue                | Output null values as default values: Number types as 0, decimal Number types as 0.0, String type as "", Character type as \u0000, array and Collection types as [], and others as {}.                                   |
| WriteBooleanAsNumber              | Write true as 1 and false as 0.                                                                                                                                                                                         |
| WriteNonStringValueAsString       | Write values of non-String types as Strings, excluding objects and data types.                                                                                                                                          |
| WriteClassName                    | Write type information when serializing.                                                                                                                                                                                |
| NotWriteRootClassName             | When WriteClassName is turned on, the type information of the root object is not output.                                                                                                                                |
| NotWriteHashMapArrayListClassName | When WriteClassName is opened, the type information of objects of type HashMap/ArrayList is not output, and the deserialization combined with UseNativeObject can save the size of the serialized result.               |
| NotWriteDefaultValue              | When the value of the field is the default value, it is not output, which can save the size of the serialized result.                                                                                                   |
| WriteEnumsUsingName               | Serialize enum using name.                                                                                                                                                                                              |
| WriteEnumUsingToString            | Serialize enum using toString method.                                                                                                                                                                                   |
| IgnoreErrorGetter                 | Ignore errors in getter methods.                                                                                                                                                                                        |
| PrettyFormat                      | Format the output.                                                                                                                                                                                                      |
| ReferenceDetection                | Turn on reference detection, which is turned off by default, which is inconsistent with fastjson 1.x.                                                                                                                   |
| WriteNameAsSymbol                 | Output field names as symbols, this only works under JSONB.                                                                                                                                                             |
| WriteBigDecimalAsPlain            | Serialize BigDecimal using toPlainString, avoiding scientific notation.                                                                                                                                                 |
| UseSingleQuotes                   | Use single quotes.                                                                                                                                                                                                      |
| MapSortField                      | Sort the KeyValue in Map by Key before output. This Feature is needed in some signature verification scenarios.                                                                                                        |
| WriteNullListAsEmpty              | Serialize null value fields of List type as empty array "[]".                                                                                                                                                           |
| WriteNullStringAsEmpty            | Serialize null value fields of String type as empty string "".                                                                                                                                                          |
| WriteNullNumberAsZero             | Serialize null value fields of Number type as 0.                                                                                                                                                                        |
| WriteNullBooleanAsFalse           | Serialize null value fields of Boolean type as false.                                                                                                                                                                   |
| NotWriteEmptyArray                | Do not output array type fields when length is 0.                                                                                                                                                                       |
| WriteNonStringKeyAsString         | Treat non-String keys in Map as String type for output.                                                                                                                                                                 |
| ErrorOnNoneSerializable           | Throw an error when serializing non-Serializable objects.                                                                                                                                                               |
| WritePairAsJavaBean               | Serialize Pair objects from Apache Commons as JavaBean.                                                                                                                                                                 |
| BrowserSecure                     | Browser security, will escape '<' '>' '(' ')' characters for output.                                                                                                                                                    |
| WriteLongAsString                 | Serialize Long as String.                                                                                                                                                                                               |
| WriteEnumUsingOrdinal             | Serialize Enum using Ordinal, the default is name.                                                                                                                                                                      |
| WriteThrowableClassName           | Include type information when serializing Throwable.                                                                                                                                                                    |
| LargeObject                       | This is a protection measure to prevent serialization of circular reference objects from consuming excessive resources.                                                                                                 |
| UnquoteFieldName                  | Output Key without quotes.                                                                                                                                                                                              |
| NotWriteSetClassName              | When WriteClassName is turned on and you don't want to output the type information of Set, use this Feature.                                                                                                            |
| NotWriteNumberClassName           | When WriteClassName is turned on and you don't want to output the type information of Number, such as the suffixes L/S/B/F/D, use this Feature.                                                                         |

# 6. Usage Examples

## 6.1 Serialization Examples

```java
// Basic usage
User user = new User("John", 25, null);
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);

// Combining multiple Features
String json2 = JSON.toJSONString(user, 
    JSONWriter.Feature.WriteNulls, 
    JSONWriter.Feature.PrettyFormat);

// Using BeanToArray feature
String json3 = JSON.toJSONString(user, JSONWriter.Feature.BeanToArray);
```

## 6.2 Deserialization Examples

```java
// Basic usage
String json = "{\"name\":\"John\",\"age\":25}";
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportSmartMatch);

// Combining multiple Features
User user2 = JSON.parseObject(json, User.class, 
    JSONReader.Feature.SupportSmartMatch, 
    JSONReader.Feature.InitStringFieldAsEmpty);
```

# 7. Best Practices

1. **Security Considerations**:
   - Do not enable `SupportAutoType` by default unless you really need to process JSON with type information
   - Be cautious when using the `FieldBased` feature with untrusted data sources

2. **Performance Optimization**:
   - For large data serialization, consider using the `BeanToArray` feature to reduce JSON size
   - Use the `MapSortField` feature when field order needs to be maintained

3. **Compatibility Handling**:
   - Use `BrowserCompatible` for web front-end scenarios to handle large integers
   - Enable `ErrorOnEnumNotMatch` in scenarios requiring strict data format to quickly identify enum mismatches

4. **Null Value Handling**:
   - Choose appropriate null value handling strategies based on business requirements, such as `WriteNulls`, `WriteNullStringAsEmpty`, etc.