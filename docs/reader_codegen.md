# 反序列化CodeGen算法介绍
fastjson2会使用codegen来优化反序列化的性能，用到的codegen技术包括：
* ASM 基于内置asm 9.2裁剪版实现的动态字节码生成类
* Annotation Process Tools(APT)

## 实现算法介绍

我们要将json反序列化为如下的Image类
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

需要生成如下的代码来快速将json中的name和字段关联起来：
```java
public final class Image_FASTJOSNReader
    extends com.alibaba.fastjson2.reader.ObjectReader5 {

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
                // 'd' | ('h' << 8) | ('"' << 16) | (':' << 24) == 975333492
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

相关方法在JSONReader中的实现
```java
class JSONReaderUTF8 implements JSONReader {
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

    @Override
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
