JSONReader是fastjson2读取JSON数据的底层实现，为了方便不同的输入，分别提供了如下实现：

* JSONReaderUTF16 用于处理输入为char[]的输入，在JDK8下， String会转成char[]，使用JSONReaderUTF16实现；在JDK 9或者更高版本，如果coder=1，也是会使用这个实现。
* JSONReaderUTF8 用于处理输入为utf8编码的byte[]
* JSONReaderASCII是JSONReaderUTF8的派生类，用于处理JDK 9之后coder=0的优化
* JSONReaderJSONB 如果输入是jsonb格式数据时，使用JSONReaderJSONB实现，通过相同的API实现两套协议，一套API

```java
class JSONReader { }

class JSONReaderUTF16 extends JSONReader {
    char[] chars;
}

class JSONReaderUTF8 extends JSONReader {
    byte[] bytes;
}

class JSONReaderASCII extends JSONReaderUTF8 { }

class JSONReaderJSONB extends JSONReader {}
```
