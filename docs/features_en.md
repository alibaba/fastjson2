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

# 4. JSONReader.Feature Introduction

| Core Parsing and Object Mapping Mechanism | |
|---------------------------------|-----------------------------------------------------------------------------------------------------|
| FieldBased | Based on field deserialization. If not configured, defaults to public fields and getter methods for deserialization. When configured, it deserializes based on non-static fields (including private). Deserialization is safer under the FieldBased configuration. |
| UseDefaultConstructorAsPossible | Use the default constructor as much as possible. If this option is not enabled while FieldBased is open, it may use Unsafe.allocateInstance to instantiate objects. |
| UseNativeObject | Defaults to using JSONObject and JSONArray. When configured, it will use LinkedHashMap and ArrayList. |
| SupportArrayToBean | Supports mapping arrays to Beans. |
| DisableReferenceDetect | Disable reference detection. |
| IgnoreCheckClose | Ignore resource cleanup checks. |

| Type Safety and Polymorphism | |
|---------------------------------|-----------------------------------------------------------------------------------------------------|
| SupportAutoType | Supports automatic typing. To read JSON data with \"@type\" type information, SupportAutoType must be explicitly enabled. |
| SupportClassForName | Supports fields of Class type, using Class.forName. For security, this is disabled by default. |
| ErrorOnNotSupportAutoType | Throw an error when encountering AutoType (ignored by default). |
| IgnoreAutoTypeNotMatch | Ignore AutoType mismatch. |

| Fault Tolerance and Compatibility Parsing |    |
|---------------------------------|-------------------------------------------------------------------|
| SupportSmartMatch | Defaults to exact camel case match. When enabled, it can intelligently recognize five cases: camel, upper, pascal, snake, and Kebab. |
| AllowUnQuotedFieldNames | Supports field names without double quotes. |
| DisableSingleQuote | Disallow the use of single quotes in keys and values. |
| NonStringKeyAsString | Treat non-String keys as Strings. |
| DuplicateKeyValueAsArray | The value of a duplicate key is combined into an array instead of being replaced. |
| IgnoreNoneSerializable | Ignore non-Serializable fields during deserialization. |
| NullOnError | Return null on deserialization error. |
| NonErrorOnNumberOverflow | Ignore number overflow errors. |
| DisableStringArrayUnwrapping | Disable unwrapping of single-element string arrays (["value"] is parsed as \"value\" by default; when enabled, it outputs as ["value"]). |

| Data Conversion and Fine-grained Processing |  |
|---------------------------------|---------------------------------------|
| InitStringFieldAsEmpty | Initialize String fields as empty strings \"\". |
| TrimString | Trim the read string values. |
| Base64StringAsByteArray | Deserialize Base64 format strings into byte[]. |
| UseBigDecimalForFloats | The default configuration uses BigDecimal to parse decimals. When enabled, it will use Float. |
| UseBigDecimalForDoubles | The default configuration uses BigDecimal to parse decimals. When enabled, it will use Double. |
| EmptyStringAsNull | Parse empty strings as null. |
| NonZeroNumberCastToBooleanAsTrue | Cast non-zero numbers to boolean true. |
| UseBigIntegerForInts | Parse integers as BigInteger. |
| UseLongForInts | Parse integers as Long. |
| UseDoubleForDecimals | Parse decimals as Double. |

| Strict Control and Interception | |
|---------------------------------|----------------------------------------------------------------------------------------------------|
| IgnoreSetNullValue | Ignore fields where the input is null. |
| IgnoreNullPropertyValue | Ignore null property values. |
| ErrorOnEnumNotMatch | By default, enum name mismatches are ignored. When enabled, mismatches will throw an exception. |
| ErrorOnNoneSerializable | Throw an error for non-Serializable objects. |
| ErrorOnNullForPrimitives | Throw an error when primitive types encounter null. |
| ErrorOnUnknownProperties | Throw an error for unknown properties. |

# 5. JSONWriter.Feature Introduction

| Core Serialization Mechanism |                                                                                                                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FieldBased | Based on field serialization. If not configured, defaults to public fields and getter methods for serialization. When configured, it serializes based on non-static fields (including private). |
| BeanToArray | Serialize objects into an array format like [101,"XX"], which results in a smaller size.                                                                                                        |
| ReferenceDetection | Enable reference detection. This is disabled by default, which is inconsistent with fastjson 1.x.                                                                                               |
| LargeObject | A protective measure to prevent excessive resource consumption caused by serializing objects with circular references.                                                                          |
| IgnoreErrorGetter | Ignore errors from getter methods.                                                                                                                                                              |
| IgnoreNonFieldGetter | Ignore getter methods that do not correspond to fields.                                                                                                                                         |
| OptimizedForAscii | Optimize for ASCII characters.                                                                                                                                                                  |

| Null and Default Value Control | |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| WriteNulls | Serialize and output null value fields. |
| WriteMapNullValue | Output null values in Map. |
| IgnoreEmpty | Ignore empty collections, empty strings, etc., and do not output them to JSON. |
| NotWriteDefaultValue | Do not output when the field's value is the default value; this saves the size of the serialized result. |
| NotWriteEmptyArray | Do not output array type fields when the length is 0. (\"@Deprecated\", recommend using IgnoreEmpty) |
| NullAsDefaultValue | Output null values as default values: Number of integer type outputs as 0, Number of decimal type outputs as 0.0, Boolean type outputs as false, String type outputs as "", Character type outputs as \u0000, array and Collection types output as [], and other types output as {}. |
| WriteNullListAsEmpty | Serialize null values of List type fields as empty arrays \"[]\". |
| WriteNullStringAsEmpty | Serialize null values of String type fields as empty strings \"\". |
| WriteNullNumberAsZero | Serialize null values of Number type fields as 0. |
| WriteNullBooleanAsFalse | Serialize null values of Boolean type fields as false. |

| Serialization Type Information | |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------|
| WriteClassName | Output type information during serialization. |
| NotWriteRootClassName | When WriteClassName is enabled, do not output the type information of the root object. |
| NotWriteHashMapArrayListClassName | When WriteClassName is enabled, do not output type information for objects of HashMap/ArrayList type. Combined with UseNativeObject during deserialization, it saves the serialization result size. |
| NotWriteSetClassName | When WriteClassName is enabled and you do not want to output Set type information, use this Feature. |
| NotWriteNumberClassName | When WriteClassName is enabled and you do not want to output Number type information, such as L/S/B/F/D suffixes, use this Feature. |
| WriteThrowableClassName | Include type information when serializing Throwable objects. |

| Type-Specific Conversion Strategies | |
|-----------------------------------|----------------------------------------------------------|
| WriteEnumsUsingName | Serialize enums using name. |
| WriteEnumUsingToString | Serialize enums using the toString method. |
| WriteEnumUsingOrdinal | Serialize enums using Ordinal; default is name. |
| WriteByteArrayAsBase64 | Convert byte arrays to Base64. |
| WriteBooleanAsNumber | Output true as 1, false as 0. |
| WriteLongAsString | Serialize Long as String. |
| WriteBigDecimalAsPlain | Serialize BigDecimal using toPlainString to avoid scientific notation. |
| WriteFloatSpecialAsString | When enabled, NaN/Infinity will be serialized as \"NaN\", \"Infinity\", \"-Infinity\". |
| WriteNonStringValueAsString | Output non-String type values as Strings, excluding object and array data types. |
| WritePairAsJavaBean | Serialize Pair objects from the Apache Commons package as JavaBeans. |
| WriterUtilDateAsMillis | Convert Date to millisecond timestamp. |

| Structure and Formatting Beautification | |
|-----------------------------------|-------------------------------------------------|
| PrettyFormat | Format output. |
| PrettyFormatWith2Space | 2-space formatting indentation. |
| PrettyFormatWith4Space | 4-space formatting indentation. |
| MapSortField | Sort Map KeyValues by Key before outputting. Required in some signature verification scenarios. |
| SortMapEntriesByKeys | Sort Map by Key. |
| UnquoteFieldName | Output Key without quotes. |
| UseSingleQuotes | Use single quotes. |
| WriteNameAsSymbol | Output field names as symbols; this only works under JSONB. |
| WriteNonStringKeyAsString | Output non-String type Keys in Map as String types. |

| Security and Cross-Platform Compatibility | |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------|
| BrowserCompatible | Output integers that greatly exceed JavaScript's supported range in string format. |
| BrowserSecure | Browser security; will escape '<' '>' '(' ')' characters for output. |
| EscapeNoneAscii | Escape characters beyond the 7-bit ASCII range (such as Chinese) using specific escaping. |
| ErrorOnNoneSerializable | Throw an error when serializing non-Serializable objects. |
| IgnoreNoneSerializable | Ignore non-Serializable type fields during serialization. |

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

# 7. 1.x Feature Migration Guide

## 7.1 Features Enabled by Default

In fastjson 1.x, the default enabled features are as follows:

**Serialization**
* `SerializerFeature.QuoteFieldNames`
* `SerializerFeature.SkipTransientField`
* `SerializerFeature.WriteEnumUsingName`
* `SerializerFeature.SortField`

**Deserialization**
* `Feature.AutoCloseSource`
* `Feature.InternFieldNames`
* `Feature.UseBigDecimal`
* `Feature.AllowUnQuotedFieldNames`
* `Feature.AllowSingleQuotes`
* `Feature.AllowArbitraryCommas`
* `Feature.SortFeidFastMatch`
* `Feature.IgnoreNotMatch`

In fastjson 2.x, **all features are OFF by default**.

## 7.2 Changes from 1.x to 2.x

**Serialization**:
* `QuoteFieldNames`: Enabled by default in 2.x; no configuration required. And 2.x supports the `UnquoteFieldName` feature.
* `UseISO8601DateFormat`: Replaced in 2.x by:
    - `JSON.configWriterDateFormat("iso8601")` (global)
    - `JSON.toJSONString(bean, "iso8601")` 
    - `@JSONField(format = "iso8601")` 
* `SkipTransientField`: Enabled by default in 2.x. To disable:
    - JVM system property `-Dfastjson2.writer.skipTransient=false`
    - `JSONFactory.setDefaultSkipTransient(false)`
    - `@JSONType(skipTransient = false)`
    - `@JSONField(skipTransient = false)`
* `SortField`: Enabled by default in 2.x; no configuration required.
* `WriteDateUseDateFormat`:  Alternatives in 2.x:
    - `JSON.toJSONString(bean, "millis")`
    - `JSONWriter.Feature.WriterUtilDateAsMillis` (since 2.0.58)
* `DisableCircularReferenceDetect`: 1.x detects circular refs by default; 2.x does not. To turn detection on in 2.x use `ReferenceDetection`.
* `WriteEnumUsingName`: Disabled by default in 2.x.
* `WriteSlashAsSpecial`: Not supported in 2.x.
* `WriteTabAsSpecial` & `DisableCheckSpecialChar`: Already deprecated in 1.x; removed in 2.x.
* All other 1.x serialization features remain unchanged.

**Deserialization**:
* `AllowArbitraryCommas`: 2.x uses strict syntax; multiple commas are not allowed.
* `AllowComment`: Enabled by default in 2.x; no configuration required.
* `AllowISO8601DateFormat`: Enabled by default in 2.x. Explicit settings:
    - `JSON.configReaderDateFormat("iso8601")`
    - `JSON.parseObject(str, Bean.class, "iso8601")`
* `AllowSingleQuotes`: Enabled by default in 2.x; no configuration required.
* `AutoCloseSource` (throw on incomplete JSON): Enabled by default in 2.x; no configuration required.
* `CustomMapDeserializer`: Not supported in 2.x.
* `DisableCircularReferenceDetect`: Renamed to `DisableReferenceDetect`.
* `DisableFieldSmartMatch`: Replaced by `SupportSmartMatch`. (Smart matching was ON in 1.x, OFF by default in 2.x.)
* `DisableSpecialKeyDetect`: Enabled by default in 2.x; no configuration required.
* `IgnoreAutoType`: 2.x disables AutoType by default; safe out-of-the-box.
* `IgnoreNotMatch`: Enabled by default in 2.x; no configuration required.
* `OrderedField` (keep declaration order for JSONObject / JSONArray): Enabled by default in 2.x; no configuration required.
* `SupportNonPublicField`: Use `FieldBased` in 2.x.
* `SafeMode`: 2.x: JVM system property `-Dfastjson2.parser.safeMode=true`.
* `TrimStringFieldValue`: Use `JSONReader.Feature.TrimString` in 2.x.
* `UseBigDecimal`: Split into `UseBigDecimalForFloats` and `UseBigDecimalForDoubles` in 2.x.
* `UseNativeJavaObject`: Renamed to `UseNativeObject`.
* `UseObjectArray` (parse JSON arrays as `Object[]` instead of `ArrayList`): Not supported in 2.x.
* All other 1.x deserialization features remain unchanged.

# 8. Best Practices

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
