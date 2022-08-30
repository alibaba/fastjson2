
JSONWriter是fastjson2序列化的底层实现，针对toJSONString和toJSONByte两种场景，会使用JSONWriterUTF8和JSONWriterUTF16两种实现。

* JSONWriterUTF16 当使用JSON.toJSONString时，缺省使用JSONWriterUTF16。
* JSONWriterUTF8 当使用JSON.toJSONByte时，缺省使用JSONWriterUTF8，在使用JSON.toJSONString结合JSONWriter.Feature.OptimizedForAscii使用时，也会用JSONWriterUTF8实现。
* JSONWriterPretty 当JSONWriter.Feature.PrettyFormat启用时，会使用JSONWriterPretty包装一个JSONWriter实现
* JSONWriterJSONB 如果序列化的结果是jsonb格式，使用JSONWriterJSONB实现，通过相同的API实现两套协议，一套API

```java
class JSONWriter { }

class JSONWriterUTF8 extends JSONWriter { }

class JSONWriterUTF16 extends JSONWriter { }

final class JSONWriterPretty extends JSONWriter {
    JSONWriter jsonWriter;

    JSONWriterPretty(JSONWriter jsonWriter) {
        this.jsonWriter = jsonWriter;
    }
}

class JSONWriterJSONB extends JSONWriter { }
```
