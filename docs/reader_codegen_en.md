# Introduction to the Deserialization Field-Matching Algorithm
fastjson2 uses field-name hashing and field-name prefix byte matching to optimize deserialization performance. This repository is a trimmed fork based on fastjson2 2.0.63, keeping only the `core` module. The compile-time Annotation Processing Tools (APT) code generation module is not part of this repository; deserialization code is not generated at compile time.

## 1. Introduction to the Field-Matching Algorithm

![image](images/reader_codegen_01.png)

In the diagram above, Algorithm 1 is a conventional implementation; Algorithm 2 is fastjson2's implementation (originally introduced by dsljson and adopted by fastjson); Algorithm 3 is a newly introduced implementation.

We want to deserialize a JSON into the following `Image` class:
```java
@Data
public class Image {
    private int height;
    private Size size;
    private String title;
    private String uri;
    private int width;
}
```

When matching field names, fastjson2 does not read the whole key and compare it as a string. `JSONReader` provides `readFieldNameHashCode()` to compute a field-name hash, and `JSONReaderUTF8` also provides `nextIfName4Match*` fast-matching methods that compare the leading bytes of the key as a single `int`. The field dispatch built on these primitives looks conceptually like this:
```java
public final class Image_FASTJSONReader {
    public Object readObject(
            com.alibaba.fastjson2.JSONReader jsonReader,
            java.lang.reflect.Type fieldType,
            Object fieldName,
            long features
    ) {
        Image object = new Image();

        while (!jsonReader.nextIfObjectEnd()) {
            switch (jsonReader.getRawInt()) {
                // '"' | ('w' << 8) | ('i' << 16) | ('d' << 24) == 1684633378
                // 't' | ('h' << 8) | ('"' << 16) | (':' << 24) == 975333492
                case 1684633378: // "wid
                    if (jsonReader.nextIfName4Match5(975333492)) { // th":
                        object.setWidth(
                                jsonReader.readInt32Value()
                        );
                        continue;
                    }
                    break;
                // '"' | ('h' << 8) | ('e' << 16) | ('i' << 24) == 1768253474
                // 'g' | ('h' << 8) | ('t' << 16) | ('"' << 24) == 578054247
                case 1768253474: // "hei
                    if (jsonReader.nextIfName4Match6(578054247)) { // ght"
                        object.setHeight(
                                jsonReader.readInt32Value()
                        );
                        continue;
                    }
                    break;
               // '"' | ('u' << 8) | ('r' << 16) | ('i' << 24) == 1769108770
                case 1769108770: // "uri"
                    if (jsonReader.nextIfName4Match3()) {
                        object.setUri(
                                jsonReader.readString()
                        );
                        continue;
                    }
                    break;
                // ...
                default:
                    break;
            }
            // ....
        }
        return object;
    }
}
```

The implementation of the related methods in `JSONReader` is as follows:
```java
class JSONReaderUTF8 extends JSONReader {
    public final int getRawInt() {
        if (offset + 3 < bytes.length) {
            return UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 1);
        }
        return 0;
    }

    public boolean nextIfName4Match3() {
        offset += 5;

        if (bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        // ...

        return true;
    }

    public final boolean nextIfName4Match4(byte c4) {
        offset += 6;
        if (bytes[offset - 3] != c4 || bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }
        // ...
        return true;
    }
    
    public boolean nextIfName4Match5(int name1) {
        offset += 7;
        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name1) {
            return false;
        }
        // ...
        return true;
    }
    
    public boolean nextIfName4Match6(int name1) {
        offset += 8;
        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name1 || bytes[offset - 1] != ':') {
            return false;
        }
        // ...
        return true;
    }
}
```

The advantage of this implementation is that it doesn't need to read the entire key and compare it as a string. Instead, it reads an integer value from the leading bytes of the key and uses a `switch` statement to route to the corresponding field's processing logic.

## 2. Implementation Notes
*   Field hash matching: `JSONReader#readFieldNameHashCode`, `JSONReaderUTF8#getRawInt`, `JSONReaderUTF8#nextIfName4Match*`
*   ObjectReader creation: `com.alibaba.fastjson2.reader.ObjectReaderCreator`, which builds field accessors based on reflection and `LambdaMetafactory`
*   Embedded ASM (`com.alibaba.fastjson2.internal.asm`, a trimmed ASM 9.2) is used for constructor parameter name lookup: `ASMUtils#lookupParameterNames`
*   Compile-time APT code generation (the codegen module) is not part of this repository. The `@JSONCompiler` and `@JSONCompiled` annotations exist as files only; no deserialization code is generated at compile time.
